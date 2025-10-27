package com.flex.interpre.domain.onboarding.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flex.interpre.domain.onboarding.dto.OnboardingChatRequest;
import com.flex.interpre.domain.onboarding.dto.OnboardingChatResponse;
import com.flex.interpre.domain.onboarding.dto.OnboardingResult;
import com.flex.interpre.domain.onboarding.entity.OnboardingSession;
import com.flex.interpre.domain.onboarding.model.OnboardingSessionCache;
import com.flex.interpre.domain.onboarding.repository.OnboardingSessionCacheRepository;
import com.flex.interpre.domain.onboarding.repository.OnboardingSessionRepository;
import com.flex.interpre.domain.user.entity.JobSeeker;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.repository.JobSeekerRepository;
import com.flex.interpre.global.constant.Area;
import com.flex.interpre.global.constant.JobFirst;
import com.flex.interpre.global.constant.JobSecond;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingAIService {

    private final BedrockRuntimeClient bedrockClient;
    private final OnboardingSessionCacheRepository cacheRepository;
    private final OnboardingSessionRepository sessionRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.bedrock.model-id}")
    private String modelId;

    public OnboardingChatResponse chat(User user, OnboardingChatRequest request) {
        // Redis에서 세션 조회 또는 생성
        OnboardingSessionCache session = cacheRepository.findByUserId(user.getId())
                .orElseGet(() -> createNewSession(user.getId()));

        // 사용자 메시지를 히스토리에 추가
        session.addMessage("user", request.message());

        // LLM에게 전체 대화 컨텍스트와 함께 요청
        String aiResponse = callBedrockWithHistory(session);

        // AI 응답을 히스토리에 추가
        session.addMessage("assistant", aiResponse);

        // 완료 여부 체크 (AI 응답에서 특수 태그로 판단)
        boolean isCompleted = aiResponse.contains("[ONBOARDING_COMPLETE]");
        session.setCompleted(isCompleted);

        // Redis에 저장
        cacheRepository.save(session);

        // 완료된 경우 결과 추출 및 저장
        OnboardingResult result = null;
        if (isCompleted) {
            result = extractResultFromResponse(aiResponse);
            saveToDatabase(session);
            updateJobSeekerInfo(user, result);
        }

        // 사용자에게 보여줄 응답 (특수 태그 제거)
        String cleanResponse = cleanResponse(aiResponse);

        return OnboardingChatResponse.builder()
                .aiResponse(cleanResponse)
                .currentStep(isCompleted ? "COMPLETED" : "IN_PROGRESS")
                .isCompleted(isCompleted)
                .result(result)
                .build();
    }

    private OnboardingSessionCache createNewSession(UUID userId) {
        OnboardingSessionCache session = OnboardingSessionCache.builder()
                .userId(userId.toString())
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .messages(new ArrayList<>())
                .build();

        // 첫 인사 메시지를 시스템에서 추가
        session.addMessage("assistant",
                "안녕하세요! 취업 준비를 도와드릴 AI 상담사입니다. 😊\n\n" +
                        "몇 가지 질문을 통해 회원님께 맞는 일자리를 추천해드리겠습니다.\n" +
                        "먼저, 어떤 지역에서 일하고 싶으신가요?");

        return session;
    }

    private String callBedrockWithHistory(OnboardingSessionCache session) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("anthropic_version", "bedrock-2023-05-31");
            requestBody.put("max_tokens", 2000);
            requestBody.put("temperature", 0.7);

            // 시스템 프롬프트
            requestBody.put("system", buildSystemPrompt());

            // 전체 대화 히스토리를 메시지로 전달
            requestBody.put("messages", session.getMessagesForClaude());

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            log.debug("Bedrock 요청: {}", jsonBody);

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .body(SdkBytes.fromUtf8String(jsonBody))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();
            log.debug("Bedrock 응답: {}", responseBody);

            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            return jsonResponse.get("content").get(0).get("text").asText();

        } catch (Exception e) {
            log.error("Bedrock API 호출 실패", e);
            return "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    // 프롬프트 구성
    private String buildSystemPrompt() {
        String areaList = buildEnumList(Area.values());
        String jobFirstList = buildEnumList(JobFirst.values());
        String jobSecondList = buildJobSecondList();

        return """
            당신은 구직자의 온보딩을 도와주는 친절한 취업 상담 AI입니다.
            
            ## 목표
            대화를 통해 다음 정보를 자연스럽게 파악하고 추천하세요:
            1. 희망 근무 지역
            2. 희망 직무 분야 (대/중 분류)
            
            ## 사용 가능한 옵션
            
            ### 지역 (Area):
            %s
            
            ### 직무 대분류 (JobFirst):
            %s
                    
            ### 직무 중분류 (JobSecond):
            %s
            
            ## 대화 가이드라인
            
            1. **자연스러운 대화**: 딱딱하지 않게, 친구처럼 편안하게 대화하세요
            2. **한 번에 하나씩**: 지역 → 직무 순서로 하나씩 물어보세요
            3. **경청과 공감**: 사용자 답변에 공감하고 긍정적으로 반응하세요
            4. **유연한 파악**: 정확한 용어가 아니어도 의도를 파악하여 매칭하세요
               - 예: "서울이나 경기" → SEOUL, GYEONGGI
               - 예: "연구직이나 개발자" → 연구_및_공학기술, 소프트웨어
            5. **추가 질문**: 모호한 경우 "연구 쪽과 개발 쪽 중 어느 분야가 더 끌리시나요?" 같이 물어보세요
            
            ## 완료 조건
            
            다음 정보가 모두 파악되면 대화를 마무리하세요:
            - ✅ 희망 지역 1개 이상
            - ✅ JobFirst 1개 이상
            - ✅ JobSecond 1개 이상  
            
            ## 완료 시 응답 형식
            
            모든 정보가 파악되면 다음과 같이 요약하고 **반드시 특수 태그를 포함**하세요:
            
            ```
            완벽합니다! 회원님의 정보를 정리해드릴게요 😊
            
            📍 희망 근무 지역: 서울, 경기
            💼 희망 직무: 연구_및_공학기술 > 소프트웨어
            
            [ONBOARDING_COMPLETE]
            [AREAS:SEOUL,GYEONGGI]
            [JOB_FIRST:연구_및_공학기술]
            [JOB_SECOND:소프트웨어]
            ```
            
            ## 주의사항
            
            - 대화 히스토리를 참고하여 이미 물어본 내용은 반복하지 마세요
            - 사용자가 이미 언급한 정보를 기억하고 활용하세요
            - 완료 조건이 충족되지 않았다면 부드럽게 추가 정보를 요청하세요
            - 특수 태그([...])는 시스템용이므로 응답 맨 끝에 포함하세요
            """.formatted(areaList, jobFirstList, jobSecondList);
    }

    // Area, JobFirst 공통 Enum 문자열 생성
    private String buildEnumList(Enum<?>[] values) {
        StringBuilder sb = new StringBuilder();
        for (Enum<?> e : values) {
            sb.append("- ").append(e.name()).append("\n");
        }
        return sb.toString();
    }

    // JobSecond는 상위 JobFirst 표시 포함
    private String buildJobSecondList() {
        StringBuilder sb = new StringBuilder();
        for (JobSecond js : JobSecond.values()) {
            sb.append("- ").append(js.name())
                    .append(" (").append(js.getParent().name()).append(")\n");
        }
        return sb.toString();
    }

    private OnboardingResult extractResultFromResponse(String response) {
        Set<Area> areas = new HashSet<>();
        Set<JobFirst> jobFirsts = new HashSet<>();
        Set<JobSecond> jobSeconds = new HashSet<>();

        // [AREAS:...] 파싱
        Pattern areasPattern = Pattern.compile("\\[AREAS:([^\\]]+)\\]");
        Matcher areasMatcher = areasPattern.matcher(response);
        if (areasMatcher.find()) {
            String[] areaNames = areasMatcher.group(1).split(",");
            for (String name : areaNames) {
                try {
                    areas.add(Area.valueOf(name.trim()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid Area: {}", name);
                }
            }
        }

        // [JOB_FIRST:...] 파싱
        Pattern jobFirstPattern = Pattern.compile("\\[JOB_FIRST:([^\\]]+)\\]");
        Matcher jobFirstMatcher = jobFirstPattern.matcher(response);
        if (jobFirstMatcher.find()) {
            String[] names = jobFirstMatcher.group(1).split(",");
            for (String name : names) {
                try {
                    jobFirsts.add(JobFirst.valueOf(name.trim()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid JobFirst: {}", name);
                }
            }
        }

        // [JOB_SECOND:...] 파싱
        Pattern jobSecondPattern = Pattern.compile("\\[JOB_SECOND:([^\\]]+)\\]");
        Matcher jobSecondMatcher = jobSecondPattern.matcher(response);
        if (jobSecondMatcher.find()) {
            String[] names = jobSecondMatcher.group(1).split(",");
            for (String name : names) {
                try {
                    jobSeconds.add(JobSecond.valueOf(name.trim()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid JobSecond: {}", name);
                }
            }
        }

        return OnboardingResult.builder()
                .recommendedAreas(areas)
                .recommendedJobFirsts(jobFirsts)
                .recommendedJobSeconds(jobSeconds)
                .build();
    }

    private String cleanResponse(String response) {
        // 특수 태그 제거
        return response
                .replaceAll("\\[ONBOARDING_COMPLETE\\]", "")
                .replaceAll("\\[AREAS:[^\\]]+\\]", "")
                .replaceAll("\\[JOB_FIRST:[^\\]]+\\]", "")
                .replaceAll("\\[JOB_SECOND:[^\\]]+\\]", "")
                .trim();
    }


    private void saveToDatabase(OnboardingSessionCache cache) {
        // 완료된 세션만 DB에 영구 저장
        StringBuilder conversationHistory = new StringBuilder();
        for (OnboardingSessionCache.ChatMessage msg : cache.getMessages()) {
            conversationHistory.append(msg.getRole().equals("user") ? "User: " : "Assistant: ")
                    .append(msg.getContent())
                    .append("\n\n");
        }

        OnboardingSession dbSession = OnboardingSession.builder()
                .userId(UUID.fromString(cache.getUserId()))
                .currentStep(OnboardingSession.OnboardingStep.COMPLETED)
                .conversationHistory(conversationHistory.toString())
                .completed(true)
                .createdAt(cache.getCreatedAt())
                .completedAt(LocalDateTime.now())
                .build();

        sessionRepository.save(dbSession);
        log.info("온보딩 세션 DB 저장 완료: userId={}", cache.getUserId());
    }


    public void updateJobSeekerInfo(User user, OnboardingResult result) {
        JobSeeker jobSeeker = jobSeekerRepository.findByUserIdWithUser(user.getId())
                .orElseThrow(() -> new RuntimeException("구직자 정보를 찾을 수 없습니다."));

        jobSeeker.setDesiredAreas(result.recommendedAreas());
        jobSeeker.setJobFirsts(result.recommendedJobFirsts());
        jobSeeker.setJobSeconds(result.recommendedJobSeconds());

        jobSeekerRepository.save(jobSeeker);

        log.info("구직자 온보딩 완료: userId={}, areas={}, jobFirst={}",
                user.getId(), result.recommendedAreas(), result.recommendedJobFirsts());
    }

    // 채팅 히스토리 조회
    public List<OnboardingSessionCache.ChatMessage> getChatHistory(User user) {
        return cacheRepository.findByUserId(user.getId())
                .map(OnboardingSessionCache::getMessages)
                .orElse(Collections.emptyList());
    }

    // 세션 초기화 (재시작)
    public void resetSession(User user) {
        cacheRepository.delete(user.getId());
        log.info("온보딩 세션 초기화: userId={}", user.getId());
    }
}
