package com.flex.interpre.domain.onboarding.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flex.interpre.domain.onboarding.dto.request.OnboardingChatRequest;
import com.flex.interpre.domain.onboarding.dto.request.OnboardingChoiceRequest;
import com.flex.interpre.domain.onboarding.dto.request.OnboardingConfirmRequest;
import com.flex.interpre.domain.onboarding.dto.response.OnboardingChatResponse;
import com.flex.interpre.domain.onboarding.dto.response.OnboardingResult;
import com.flex.interpre.domain.onboarding.entity.OnboardingSession;
import com.flex.interpre.domain.onboarding.model.OnboardingSessionCache;
import com.flex.interpre.domain.onboarding.repository.OnboardingSessionCacheRepository;
import com.flex.interpre.domain.onboarding.repository.OnboardingSessionRepository;
import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.jobSeeker.entity.User;
import com.flex.interpre.domain.jobSeeker.repository.JobSeekerRepository;
import com.flex.interpre.global.constant.Area;
import com.flex.interpre.global.constant.JobFirst;
import com.flex.interpre.global.constant.JobSecond;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        당신은 구직자의 온보딩을 도와주는 친절한 취업 상담 AI입니다. 😊
        
        ## 🎯 목표
        대화를 통해 구직자의 상태(A/B 그룹)에 따라 다음 정보를 자연스럽게 파악하고 추천하세요:
        1. 희망 근무 지역 (Area)
        2. 희망 직무 분야 (JobFirst, JobSecond)
        
        ## 👥 구직자 유형
        - **A그룹 (희망 분야 없음)**
              사용자가 아직 어떤 직무를 하고 싶은지 모르거나 정하지 못했습니다.
              이런 경우에는 사용자의 **성향, 경험, 흥미**를 파악하며 도와주세요.
              예시:
              - "예전에 어떤 일을 해보셨거나 흥미 있었던 경험이 있으신가요?"
              - "사람을 돕는 일, 만드는 일, 분석하는 일 중 어떤 게 더 끌리세요?"
              - "아직 정해지지 않아도 괜찮아요 😊 궁금한 건 언제든 물어보셔도 돼요!"
        
        - **B그룹 (희망 분야 있음)** 
              사용자가 이미 관심 있는 직무나 분야를 명시했습니다.
              이 경우에는 더 구체적인 선택지를 제시하며 대화를 이어가세요.
              예시:
              - "IT 분야에 관심 있으시군요 💻 세부적으로는 어떤 업무가 끌리시나요?"
              - "혹시 기존 분야 외에도 새롭게 관심 있는 일도 있을까요?"

        ## 🚦 그룹 판별 규칙
        
        1. 사용자가 직무나 분야를 언급하지 않았다면 → **A그룹**
        2. 사용자가 ‘IT’, ‘디자인’, ‘경영’, ‘교육’, ‘의료’, ‘건설’ 등
                    특정 관심 분야를 언급했다면 → **B그룹**

        LLM은 위 규칙에 따라 내부적으로 그룹을 판단하고 기억합니다.
        (⚠️ **A/B 그룹은 태그로 표시하지 마세요.** 내부적으로 기억만 하세요.)
        
        ## 🗂️ 사용 가능한 옵션 (시스템이 동적으로 주입)

        ### 지역 (Area)
        %s

        ### 직무 대분류 (JobFirst)
        %s

        ### 직무 중분류 (JobSecond)
        %s

        ※ 직무 중분류는 각 대분류(JobFirst)에 속하는 세부 분야입니다.
        예를 들어 `연구_및_공학기술`에는 `소프트웨어`, `데이터_및_정보시스템_웹_운영`, `건설_채굴_연구` 등이 포함됩니다.

        ---
        ## 🏷️ 태그 사용 규칙
        
        아래 항목들은 **시스템이 정보를 파싱해야 하므로 반드시 태그로 포함**해야 합니다.
        - [ONBOARDING_COMPLETE] : 온보딩 종료 시
        - [AREAS:지역명1,지역명2] : 사용자가 선택한 지역
        - [JOB_FIRST:직무대분류명]
        - [JOB_SECOND:직무중분류명]
        
        📌 태그는 반드시 대괄호로 감싸고, 응답의 **맨 마지막 줄**에 위치해야 합니다.
        예시:
        ```
        완벽합니다! 회원님의 정보를 정리해드릴게요 😊
        
        📍 희망 근무 지역: 서울, 경기
        💼 희망 직무: 연구_및_공학기술 > 소프트웨어
        
        [ONBOARDING_COMPLETE]
        [AREAS:SEOUL,GYEONGGI]
        [JOB_FIRST:연구_및_공학기술]
        [JOB_SECOND:소프트웨어]
        ```
        
        ---

        ## 💬 대화 가이드라인

        1. **자연스럽게 대화하세요.**  
           - 친구처럼 부드럽고 공감 어린 말투를 유지하세요.  
           - 예: “좋아요! 기술 쪽이 잘 맞으실 것 같아요 💻”

        2. **한 번에 하나씩 질문하세요.**  
           - 지역을 먼저 정하세요.
            → 직무(대분류 → 중분류)를 정하세요.

        3. **궁금증 유도 문구 포함.**  
           - 직무나 분야가 낯설어 보일 경우 “궁금하시면 물어보셔도 돼요 😊”를 꼭 덧붙이세요.  
           - 예: “데이터/웹 운영은 웹사이트와 서버 데이터를 관리하는 일이에요. 궁금하시면 물어보셔도 돼요 😊”

        4. **사용자 응답에 맞게 세부 선택 제공.**  
           - 선택지를 제공할 때는 다음 JSON 형식으로 응답합니다.
           ```json
           {
             "type": "selection",
             "message": "좋아요! 아래 분야 중 어떤 게 끌리시나요?",
             "choices": [
               {"label": "소프트웨어", "value": "소프트웨어"},
               {"label": "네트워크/보안", "value": "네트워크_시스템_및_정보보안"},
               {"label": "데이터/웹 운영", "value": "데이터_및_정보시스템_웹_운영"}
             ]
           }
           ```
           - `label`: 사용자에게 보여질 명칭  
           - `value`: 실제 Enum name() 값 (한글 그대로)
           - 프론트엔드는 `value`를 그대로 서버에 전달하여 JobSeeker 정보를 업데이트합니다.

        5. **정보 누락 시 추가 질문으로 유도.**
           - 예: “혹시 비슷한 다른 분야도 생각해보신 적 있으세요?”
           
        ---
        ## 💡 대화 흐름 예시
        
        
         0⃣  첫 질문(희망근무지역):
        ```
        직무를 설정하기 전에 물어볼게 있어요.
        어느 지역에서 일하고 싶으신가요? 
        예: 서울, 경기, 부산, 대구 등
        ```
        
        1️⃣ 다음 질문(직무):
        ```
        혹시 이미 관심 있는 직무 분야가 있으신가요?
        예: IT개발, 디자인, 경영 등
        아니면 아직 잘 모르겠고 같이 찾아보고 싶으신가요? 😊
        ```
        
        2️⃣ 사용자의 응답으로 A/B 그룹 판단
        3️⃣ 이후 그룹별 맞춤 대화
           - A그룹: 경험/성향 중심 탐색
           - B그룹: 세부 직무 선택 유도
        4️⃣ 희망 근무 지역 → 직무 대분류(JobFirst) → 중분류(JobSecond) 순으로 이어가기
        5️⃣ 모든 정보가 수집되면 태그 포함 형태로 마무리
        
        ---
        
        ## ✅ 완료 조건
        다음 정보를 모두 얻으면 대화를 종료합니다.
        - 희망 지역 1개 이상
        - JobFirst 1개 이상
        - JobSecond 1개 이상

        ## ⚙️ 주의사항
        - 대화 히스토리를 참고해 이미 한 질문은 반복하지 마세요.
        - 사용자가 이미 언급한 정보를 기억하고 활용하세요.
        - 사용자가 선택 중이면, 그 선택이 실제 Enum 값과 정합되도록 value를 출력하세요.
        - 특수 태그([...])는 시스템용이며, 응답 마지막에 위치시킵니다.
        """.formatted(areaList, jobFirstList, jobSecondList);
    }

    @Transactional
    public void confirmSelections(User user, OnboardingConfirmRequest req) {
        OnboardingResult result = OnboardingResult.builder()
                .recommendedAreas(req.areas().stream().map(this::convertToAreaEnum).filter(Objects::nonNull).collect(Collectors.toSet()))
                .recommendedJobFirsts(req.jobFirsts().stream().map(JobFirst::valueOf).collect(Collectors.toSet()))
                .recommendedJobSeconds(req.jobSeconds().stream().map(JobSecond::valueOf).collect(Collectors.toSet()))
                .build();

        updateJobSeekerInfo(user, result);
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

    @Transactional
    public OnboardingChatResponse handleChoice(User user, OnboardingChoiceRequest request) {
        // 캐시 세션 조회
        OnboardingSessionCache session = cacheRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("온보딩 세션을 찾을 수 없습니다."));

        // 선택 내용을 자연스러운 문장으로 변환해 LLM에 전달
        String userMessage = switch (request.type().toUpperCase()) {
            case "AREA" -> "저는 " + request.value() + " 지역에서 일하고 싶어요.";
            case "JOB_FIRST" -> "저는 " + request.value() + " 분야가 좋아요.";
            case "JOB_SECOND" -> "저는 " + request.value() + " 쪽이 끌려요.";
            default -> request.value();
        };
        session.addMessage("user", userMessage);

        // Bedrock 호출 → 다음 대화 응답 생성
        String aiResponse = callBedrockWithHistory(session);
        session.addMessage("assistant", aiResponse);

        // 세션 저장
        cacheRepository.save(session);

        // LLM이 [ONBOARDING_COMPLETE] 태그 보낸 경우 → DB 반영
        boolean isCompleted = aiResponse.contains("[ONBOARDING_COMPLETE]");
        if (isCompleted) {
            OnboardingResult result = extractResultFromResponse(aiResponse);
            updateJobSeekerInfo(user, result);
            session.setCompleted(true);
            cacheRepository.save(session);
        }

        // 텍스트 반환
        return OnboardingChatResponse.builder()
                .aiResponse(cleanResponse(aiResponse))
                .isCompleted(isCompleted)
                .currentStep(isCompleted ? "COMPLETED" : "IN_PROGRESS")
                .build();
    }


    /*  내부 메서드  */

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

    private void updateJobSeekerInfo(User user, OnboardingResult result) {
        JobSeeker jobSeeker = jobSeekerRepository.findByUserIdWithUser(user.getId())
                .orElseThrow(() -> new RuntimeException("구직자 정보를 찾을 수 없습니다."));

        jobSeeker.setDesiredAreas(result.recommendedAreas());
        jobSeeker.setJobFirsts(result.recommendedJobFirsts());
        jobSeeker.setJobSeconds(result.recommendedJobSeconds());

        jobSeekerRepository.save(jobSeeker);

        log.info("구직자 온보딩 완료: userId={}, areas={}, jobFirst={}",
                user.getId(), result.recommendedAreas(), result.recommendedJobFirsts());
    }

    // 한글 -> 영어 Enum 매핑
    private Area convertToAreaEnum(String value) {
        if (value == null) return null;

        String cleaned = value.trim()
                .replace("시", "")
                .replace("도", "")
                .replace(" ", "")
                .toUpperCase();

        Map<String, Area> areaMap = Map.ofEntries(
                Map.entry("서울", Area.SEOUL),
                Map.entry("서울특별", Area.SEOUL),
                Map.entry("SEOUL", Area.SEOUL),

                Map.entry("경기", Area.GYEONGGI),
                Map.entry("경기도", Area.GYEONGGI),
                Map.entry("GYEONGGI", Area.GYEONGGI),

                Map.entry("인천", Area.INCHEON),
                Map.entry("INCHEON", Area.INCHEON),

                Map.entry("강원", Area.GANGWON),
                Map.entry("GANGWON", Area.GANGWON),

                Map.entry("충북", Area.CHUNGBUK),
                Map.entry("충청북", Area.CHUNGBUK),
                Map.entry("CHUNGBUK", Area.CHUNGBUK),

                Map.entry("충남", Area.CHUNGNAM),
                Map.entry("충청남", Area.CHUNGNAM),
                Map.entry("CHUNGNAM", Area.CHUNGNAM),

                Map.entry("전북", Area.JEONBUK),
                Map.entry("전라북", Area.JEONBUK),
                Map.entry("JEONBUK", Area.JEONBUK),

                Map.entry("전남", Area.JEONNAM),
                Map.entry("전라남", Area.JEONNAM),
                Map.entry("JEONNAM", Area.JEONNAM),

                Map.entry("경북", Area.GYEONGBUK),
                Map.entry("경상북", Area.GYEONGBUK),
                Map.entry("GYEONGBUK", Area.GYEONGBUK),

                Map.entry("경남", Area.GYEONGNAM),
                Map.entry("경상남", Area.GYEONGNAM),
                Map.entry("GYEONGNAM", Area.GYEONGNAM),

                Map.entry("대전", Area.DAEJEON),
                Map.entry("DAEJEON", Area.DAEJEON),

                Map.entry("대구", Area.DAEGU),
                Map.entry("DAEGU", Area.DAEGU),

                Map.entry("광주", Area.GWANGJU),
                Map.entry("GWANGJU", Area.GWANGJU),

                Map.entry("부산", Area.BUSAN),
                Map.entry("BUSAN", Area.BUSAN),

                Map.entry("울산", Area.ULSAN),
                Map.entry("ULSAN", Area.ULSAN),

                Map.entry("세종", Area.SEJONG),
                Map.entry("SEJONG", Area.SEJONG),

                Map.entry("제주", Area.JEJU),
                Map.entry("JEJU", Area.JEJU)
        );

        // 영어 대문자 직접 시도
        try {
            return Area.valueOf(cleaned);
        } catch (IllegalArgumentException ignored) {
        }

        // 한글/혼합 매핑
        Area mapped = areaMap.getOrDefault(cleaned, null);
        if (mapped != null) return mapped;


        log.warn("⚠️ Unknown Area value: {}", value);
        return null;
    }

}

