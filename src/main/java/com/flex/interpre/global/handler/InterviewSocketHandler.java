package com.flex.interpre.global.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flex.interpre.domain.interview.dto.response.InterviewAnalysisResult;
import com.flex.interpre.domain.interview.dto.response.InterviewReportDto;
import com.flex.interpre.domain.interview.dto.response.InterviewResponse;
import com.flex.interpre.domain.interview.entity.*;
import com.flex.interpre.domain.interview.exception.InterviewExceptions;
import com.flex.interpre.domain.interview.repository.InterviewChatRepository;
import com.flex.interpre.domain.interview.repository.InterviewRepository;
import com.flex.interpre.domain.interview.repository.InterviewSessionRepository;
import com.flex.interpre.domain.interview.repository.QnaRepository;
import com.flex.interpre.domain.interview.service.InterviewService;
import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.jobSeeker.repository.JobSeekerRepository;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.recruitment.service.RecruitmentIndexService;
import com.flex.interpre.global.exception.ApiException;
import com.flex.interpre.global.module.embedding.ClovaEmbeddingService;
import com.flex.interpre.global.module.stt.ClovaGrpcSttService;
import com.flex.interpre.global.util.KoreanTextProcessor;
import com.naver.cloud.speech.grpc.NestRequest;
import io.grpc.stub.StreamObserver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterviewSocketHandler extends AbstractWebSocketHandler {
    
    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewChatRepository interviewChatRepository;
    private final ClovaEmbeddingService clovaEmbeddingService;
    private final QnaRepository qnaRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewService interviewService;
    private final ObjectMapper objectMapper;
    private final RecruitmentIndexService recruitmentIndexService;
    private final ClovaGrpcSttService clovaGrpcSttService;
    private final JobSeekerRepository jobSeekerRepository;
    private final KoreanTextProcessor koreanTextProcessor;

    private final ConcurrentHashMap<String, StreamObserver<NestRequest>> grpcStreamMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, StringBuilder> transcriptionBufferMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ScheduledFuture<?>> answerCheckTimers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ScheduledFuture<?>> forceCompleteTimers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> currentQuestions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> answerProcessed = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        
        String sessionId = getSessionId(session);
        try {
            InterviewSession interviewSession = interviewSessionRepository.findById(UUID.fromString(sessionId))
                    .orElseThrow(InterviewExceptions.INVALID_SESSION_ID::toException);
            
            //이미 이전에 시작된 인터뷰인 경우
            if (interviewSession.getCurrentQuestionNum() > 0) {
                throw InterviewExceptions.ALREADY_STARTED_INTERVIEW.toException();
            }
            
            String document = interviewSession.getContentText();

            StreamObserver<NestRequest> grpcStream = clovaGrpcSttService.startStreaming(
                    session,
                    (text) -> {
                        try {
                            String wsSessionId = session.getId();
                            transcriptionBufferMap.computeIfAbsent(wsSessionId, k -> new StringBuilder()).append(text).append(" ");

                            String rawText = transcriptionBufferMap.get(wsSessionId).toString().trim();
                            String correctedText = koreanTextProcessor.correctSpacing(rawText);

                            InterviewResponse realtimeStt = InterviewResponse.builder()
                                    .type(InterviewResponse.ResponseType.STT)
                                    .text(correctedText)
                                    .build();
                            sendSuccess(session, realtimeStt);

                            cancelAnswerCheckTimer(wsSessionId);
                            cancelForceCompleteTimer(wsSessionId);

                            answerProcessed.put(wsSessionId, false);

                            ScheduledFuture<?> checkTimer = scheduler.schedule(() -> {
                                log.info("1.5초 타이머 만료 - AI 답변 완료 판단 시작");
                                checkAndProcessAnswer(session, sessionId);
                            }, 1500, TimeUnit.MILLISECONDS);
                            answerCheckTimers.put(wsSessionId, checkTimer);

                            ScheduledFuture<?> forceTimer = scheduler.schedule(() -> {
                                log.info("5초 타이머 만료 - 강제 답변 종료");
                                forceCompleteAnswer(session, sessionId);
                            }, 5000, TimeUnit.MILLISECONDS);
                            forceCompleteTimers.put(wsSessionId, forceTimer);

                        } catch (Exception e) {
                            log.error("실시간 STT 전송 오류: {}", e.getMessage(), e);
                        }
                    }
            );

            clovaGrpcSttService.sendConfig(grpcStream);

            grpcStreamMap.put(session.getId(), grpcStream);
            transcriptionBufferMap.put(session.getId(), new StringBuilder());

            String firstQuestion = interviewService.generateQuestions(document, new ArrayList<>());

            currentQuestions.put(session.getId(), firstQuestion);
            byte[] questionAudio = interviewService.tts(firstQuestion);
            String audioBase64 = Base64.getEncoder().encodeToString(questionAudio);

            InterviewChat interviewChat = InterviewChat.builder()
                    .interviewId(interviewSession.getInterviewId())
                    .questionNum(1)
                    .question(firstQuestion)
                    .answer(null)
                    .build();

            interviewChatRepository.save(interviewChat);

            interviewSession.setContentText(document);
            interviewSession.setCurrentQuestionNum(1);
            interviewSessionRepository.save(interviewSession);

            InterviewResponse interviewResponse = InterviewResponse.builder()
                    .type(InterviewResponse.ResponseType.QUESTION)
                    .question(firstQuestion)
                    .audio(audioBase64)
                    .build();

            sendSuccess(session, interviewResponse);
            
            
        } catch (ApiException e) {
            log.error("연결 수립 오류 (ApiException): {}", e.getMessage(), e);
            sendError(session, e.getMessage());
            session.close(CloseStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            log.error("연결 수립 오류: {}", e.getMessage(), e);
            sendError(session, "서버 에러 발생: " + e.getMessage());
            session.close(CloseStatus.SERVER_ERROR);
        }
    }
    
    @Override
    public void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message) {
        try {
            String wsSessionId = session.getId();
            StreamObserver<NestRequest> grpcStream = grpcStreamMap.get(wsSessionId);

            if (grpcStream != null) {
                byte[] audioChunk = message.getPayload().array();
                clovaGrpcSttService.sendAudioData(grpcStream, audioChunk);
            } else {
                log.warn("gRPC 스트림을 찾을 수 없음: {}", wsSessionId);
            }
        } catch (Exception e) {
            log.error("오디오 처리 오류: {}", e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String wsSessionId = session.getId();

        cancelAnswerCheckTimer(wsSessionId);
        cancelForceCompleteTimer(wsSessionId);

        StreamObserver<NestRequest> grpcStream = grpcStreamMap.remove(wsSessionId);
        if (grpcStream != null) {
            try {
                grpcStream.onCompleted();
            } catch (Exception e) {
                log.warn("gRPC 스트림 종료 오류: {}", e.getMessage());
            }
        }

        transcriptionBufferMap.remove(wsSessionId);
        currentQuestions.remove(wsSessionId);
        answerProcessed.remove(wsSessionId);
    }

    private void cancelAnswerCheckTimer(String wsSessionId) {
        ScheduledFuture<?> existingTimer = answerCheckTimers.remove(wsSessionId);
        if (existingTimer != null && !existingTimer.isDone()) {
            existingTimer.cancel(false);
        }
    }

    private void cancelForceCompleteTimer(String wsSessionId) {
        ScheduledFuture<?> existingTimer = forceCompleteTimers.remove(wsSessionId);
        if (existingTimer != null && !existingTimer.isDone()) {
            existingTimer.cancel(false);
        }
    }

    private void sendSuccess(WebSocketSession session, Object data) throws IOException {
        String json = objectMapper.writeValueAsString(
                Map.of("success", true, "data", data)
        );
        session.sendMessage(new TextMessage(json));
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        String json = objectMapper.writeValueAsString(
                Map.of("success", false, "message", message)
        );
        session.sendMessage(new TextMessage(json));
    }

    private void checkAndProcessAnswer(WebSocketSession session, String sessionId) {
        try {
            String wsSessionId = session.getId();

            if (Boolean.TRUE.equals(answerProcessed.get(wsSessionId))) {
                return;
            }

            String currentQuestion = currentQuestions.get(wsSessionId);
            if (currentQuestion == null) {
                log.warn("현재 질문을 찾을 수 없음: {}", wsSessionId);
                return;
            }

            StringBuilder buffer = transcriptionBufferMap.get(wsSessionId);
            String rawAnswer = buffer != null ? buffer.toString().trim() : "";

            if (rawAnswer.isEmpty()) {
                return;
            }

            String currentAnswer = koreanTextProcessor.correctSpacing(rawAnswer);

            boolean isComplete = interviewService.isAnswerComplete(currentQuestion, currentAnswer);

            if (isComplete) {
                log.info("AI 판단: 답변 완료 - 다음 질문 생성 시작");

                answerProcessed.put(wsSessionId, true);

                cancelAnswerCheckTimer(wsSessionId);
                cancelForceCompleteTimer(wsSessionId);

                InterviewResponse answerCompleteSignal = InterviewResponse.builder()
                        .type(InterviewResponse.ResponseType.ANSWER_COMPLETE)
                        .build();
                sendSuccess(session, answerCompleteSignal);

                transcriptionBufferMap.put(wsSessionId, new StringBuilder());

                sendQuestion(session, sessionId, currentAnswer);
            }

        } catch (Exception e) {
            log.error("답변 완료 확인 오류: {}", e.getMessage(), e);
        }
    }

    private void forceCompleteAnswer(WebSocketSession session, String sessionId) {
        try {
            String wsSessionId = session.getId();

            if (Boolean.TRUE.equals(answerProcessed.get(wsSessionId))) {
                log.info("5초 타이머 만료했지만 이미 질문 생성됨 - 스킵");
                return;
            }

            StringBuilder buffer = transcriptionBufferMap.get(wsSessionId);
            String rawAnswer = buffer != null ? buffer.toString().trim() : "";

            if (rawAnswer.isEmpty()) {
                log.warn("5초 타이머 만료했지만 답변이 없음");
                return;
            }

            log.info("5초 타이머 만료 - 강제 답변 종료 처리");

            answerProcessed.put(wsSessionId, true);

            String currentAnswer = koreanTextProcessor.correctSpacing(rawAnswer);

            cancelAnswerCheckTimer(wsSessionId);
            cancelForceCompleteTimer(wsSessionId);

            InterviewResponse answerCompleteSignal = InterviewResponse.builder()
                    .type(InterviewResponse.ResponseType.ANSWER_COMPLETE)
                    .build();
            sendSuccess(session, answerCompleteSignal);

            transcriptionBufferMap.put(wsSessionId, new StringBuilder());

            sendQuestion(session, sessionId, currentAnswer);

        } catch (Exception e) {
            log.error("강제 답변 종료 오류: {}", e.getMessage(), e);
        }
    }
    
    private String getSessionId(WebSocketSession session) {
        
        String uri = Objects.requireNonNull(session.getUri()).toString();
        
        return UriComponentsBuilder.fromUriString(uri)
                .build()
                .getQueryParams()
                .getFirst("sessionId");
    }
    
    @Transactional
    protected void sendQuestion(WebSocketSession session, String sessionId, String transcription) throws IOException {
        
        InterviewSession interviewSession = interviewSessionRepository.findById(UUID.fromString(sessionId))
                .orElseThrow(InterviewExceptions.INVALID_SESSION_ID::toException);
        
        Integer currentQuestionNumber = interviewSession.getCurrentQuestionNum();
        UUID interviewId = interviewSession.getInterviewId();
        
        //대답에 해당하는 질문 가져오기
        InterviewChat currentChat = interviewChatRepository.findByQuestionNum(currentQuestionNumber);
        
        //응답 저장
        currentChat.setAnswer(transcription);
        interviewChatRepository.save(currentChat);
        
        Long duration = checkInterviewEnd(interviewSession);
        if (duration >= 1) {
            finishInterview(session, sessionId, transcription, duration);
            return;
        }

        List<InterviewChat> chatHistory = interviewChatRepository
                .findByInterviewIdOrderByQuestionNum(interviewId);

        String nextQuestion = interviewService.generateQuestions(interviewSession.getContentText(), chatHistory);

        InterviewChat nextChat = InterviewChat.builder()
                .interviewId(interviewId)
                .questionNum(currentQuestionNumber + 1)
                .question(nextQuestion)
                .answer(null)
                .build();

        interviewChatRepository.save(nextChat);

        interviewSession.setCurrentQuestionNum(currentQuestionNumber + 1);
        interviewSessionRepository.save(interviewSession);

        currentQuestions.put(session.getId(), nextQuestion);

        byte[] AudioData = interviewService.tts(nextQuestion);
        String audioBase64 = Base64.getEncoder().encodeToString(AudioData);

        InterviewResponse response = InterviewResponse.builder()
                .type(InterviewResponse.ResponseType.QUESTION)
                .question(nextQuestion)
                .audio(audioBase64)
                .build();

        sendSuccess(session, response);
        
        
    }
    
    private Long checkInterviewEnd(InterviewSession interviewSession) {
        LocalDateTime start = interviewSession.getStartedAt();
        
        return Duration.between(start, LocalDateTime.now()).toMinutes();
    }
    
    @Transactional
    protected void finishInterview(WebSocketSession session, String sessionId, String lastTranscription, Long duration)
            throws IOException {
        // 인터뷰 세션
        InterviewSession interviewSession = interviewSessionRepository.findById(UUID.fromString(sessionId))
                .orElseThrow(InterviewExceptions.INVALID_SESSION_ID::toException);
        
        Interview interview = interviewRepository.findById(interviewSession.getInterviewId())
                .orElseThrow(InterviewExceptions.INVALID_SESSION_ID::toException);
        
        // 질의응답 내역
        List<InterviewChat> chatHistory = interviewChatRepository.findByInterviewIdOrderByQuestionNum(
                interviewSession.getInterviewId());
        
        //Redis에 저장된 채팅 기록 Qna 객체로 변환
        List<Qna> qnas = chatHistory.stream().map(chat -> Qna.from(chat, interview)).toList();
        
        String fullTranscript = qnas.stream()
                .map(qna -> String.format("질문: %s\n답변: %s", qna.getQuestion(), qna.getAnswer()))
                .collect(Collectors.joining("\n\n"));
        
        // 인터뷰 기록 인베딩
        List<Double> embedding = clovaEmbeddingService.embed(fullTranscript);
        interview.setEmbedding(embedding);
        interview.setTitle(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")) + " 면접 기록");
        
        //면접 분석
        InterviewAnalysisResult analysisResult = interviewService.analyzeInterview(fullTranscript);
        
        /* -- 온보딩 데이터 기반 가중치 반영 -- TODO: 가중치 점수 테스트 */
        JobSeeker jobSeeker = jobSeekerRepository.findByIdWithInterviews(interviewSession.getJobSeekerId());
        
        // 유저의 누적 임베딩 (없으면 인터뷰 임베딩 사용)
        List<Double> searchVector = (jobSeeker.getCumulativeEmbedding() != null && !jobSeeker.getCumulativeEmbedding().isEmpty())
                ? jobSeeker.getCumulativeEmbedding()
                : embedding;
        
        List<Recruitment> candidates = recruitmentIndexService.searchByVector(searchVector, 40);
        Map<UUID, Double> scored = new HashMap<>(); // 공고문, 추가점수를 가지는 map
        
        for (Recruitment job : candidates) {
            double score = 1.0; // 기본 점수
            
            // 희망근무지역이 같은 공고에 추가 점수 반영
            boolean matchesArea = !Collections.disjoint(jobSeeker.getDesiredAreas(), job.getJobAreas());
            if (matchesArea) {
                score += 0.4;
            }
            
            // 희망근무지역 (하나라도 일치하면 가점)
            boolean matchesJob =
                    !Collections.disjoint(jobSeeker.getJobFirsts(), job.getJobFirsts()) ||
                            !Collections.disjoint(jobSeeker.getJobSeconds(), job.getJobSeconds()) ||
                            !Collections.disjoint(jobSeeker.getJobThirds(), job.getJobThirds());
            if (matchesJob) {
                score += 0.6;
            }
            
            scored.put(job.getId(), score);
        }
        
        // 최종 정렬 후 상위 5개 추천
        List<Recruitment> finalRecommendations = candidates.stream()
                .sorted((a, b) -> Double.compare(scored.get(b.getId()), scored.get(a.getId())))
                .limit(5)
                .toList();
        
        // 면접 리포트 생성
        InterviewReport interviewReport = InterviewReport.builder()
                .interview(interview)
                .aiFeedback(analysisResult.aiFeedback())
                .strengths(analysisResult.strengths())
                .weaknesses(analysisResult.weaknesses())
                .competencyScores(analysisResult.competencyScores())
                .recommendations(finalRecommendations)
                .build();
        
        interview.setInterviewReport(interviewReport);
        
        //전부 저장
        qnaRepository.saveAll(qnas);
        
        interview.setDurationSecond(duration);
        interviewRepository.save(interview);
        
        List<Double> newEmbedding = interview.getEmbedding();
        
        if (newEmbedding != null && !newEmbedding.isEmpty()) {
            List<Double> currentCumulative = jobSeeker.getCumulativeEmbedding();
            int oldCount = jobSeeker.getInterviews().size() - 1;
            
            if (!currentCumulative.isEmpty() && oldCount > 0) {
                List<Double> updated = IntStream.range(0, newEmbedding.size())
                        .mapToObj(i -> (currentCumulative.get(i) * oldCount + newEmbedding.get(i)) / (oldCount + 1))
                        .toList();
                jobSeeker.setCumulativeEmbedding(updated);
            } else {
                // 첫 인터뷰
                jobSeeker.setCumulativeEmbedding(newEmbedding);
            }
            jobSeekerRepository.save(jobSeeker);
        }
        
        String closingMessage = String.format(
                "수고하셨습니다. %d분간 총 %d개의 질문에 답변해 주셨습니다. 오늘 면접 참여해 주셔서 감사합니다.",
                duration,
                chatHistory.size()
        );
        
        byte[] audioData = interviewService.tts(closingMessage);
        String audioBase64 = Base64.getEncoder().encodeToString(audioData);

        InterviewResponse response = InterviewResponse.builder()
                .type(InterviewResponse.ResponseType.END)
                .question(closingMessage)
                .audio(audioBase64)
                .report(InterviewReportDto.from(interviewReport))
                .build();

        sendSuccess(session, response);
        
        interviewChatRepository.deleteAll(chatHistory);
        interviewSessionRepository.delete(interviewSession);
        
    }
}
