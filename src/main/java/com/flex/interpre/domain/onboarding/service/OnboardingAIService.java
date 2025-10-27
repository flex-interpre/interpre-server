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
import com.flex.interpre.global.constant.JobThird;
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

    private String buildSystemPrompt() {
        return """
            당신은 구직자의 온보딩을 도와주는 친절한 취업 상담 AI입니다.
            
            ## 목표
            대화를 통해 다음 정보를 자연스럽게 파악하고 추천하세요:
            1. 희망 근무 지역
            2. 희망 직무 분야 (대/중/소 분류)
            
            ## 사용 가능한 옵션
            
            ### 지역 (Area):
            SEOUL, BUSAN, DAEGU, INCHEON, GWANGJU, DAEJEON, ULSAN, SEJONG,
            GYEONGGI, GANGWON, CHUNGBUK, CHUNGNAM, JEONBUK, JEONNAM, GYEONGBUK, GYEONGNAM, JEJU
            
            ### 직무 대분류 (JobFirst):
                경영_사무_금융_보험,
                연구_및_공학기술,
                교육_법률_사회복지_경찰_소방_및_군인,
                보건_의료,
                예술_디자인_방송_스포츠,
                미용_여행_숙박_음식_경비_돌봄_청소,
                영업_판매_운전_운송,
                건설_채굴,
                설치_정비_생산_기계_금속_재료,
                설치_정비_생산_전기_전자_정보통신,
                설치_정비_생산_화학_환경_섬유_의복_식품가공,
                설치_정비_생산_인쇄_목재_공예_및_제조_단순,
                농림어업직
                    
                    
            ### 직무 중분류 (JobSecond):
                    행정_경영_금융_보험_관리직(JobFirst.경영_사무_금융_보험),
                    교육_법률_복지_의료_예술_방송_정보통신_등_전문서비스_관리직(JobFirst.경영_사무_금융_보험),
                    미용_여행_숙박_음식_등_개인서비스_및_영업_판매_운송_관리직(JobFirst.경영_사무_금융_보험),
                    건설_채굴_제조_생산_관리직(JobFirst.경영_사무_금융_보험),
                    행정_경영_회계_광고_상품기획_전문가(JobFirst.경영_사무_금융_보험),
                    정부_행정_사무(JobFirst.경영_사무_금융_보험),
                    경영지원_사무(JobFirst.경영_사무_금융_보험),
                    회계_경리_사무(JobFirst.경영_사무_금융_보험),
                    무역_운송_자재_구매_생산_품질_사무(JobFirst.경영_사무_금융_보험),
                    안내_접수_고객상담_사무(JobFirst.경영_사무_금융_보험),
                    통계_비서_사무보조_기타_사무(JobFirst.경영_사무_금융_보험),
                    금융_보험_전문가(JobFirst.경영_사무_금융_보험),
                    금융_보험_사무_및_영업(JobFirst.경영_사무_금융_보험),
                    인문_사회_자연_생명과학_연구_및_시험(JobFirst.연구_및_공학기술),
                    컴퓨터하드웨어_통신공학(JobFirst.연구_및_공학기술),
                    컴퓨터시스템(JobFirst.연구_및_공학기술),
                    소프트웨어(JobFirst.연구_및_공학기술),
                    네트워크_시스템_및_정보보안(JobFirst.연구_및_공학기술),
                    데이터_및_정보시스템_웹_운영(JobFirst.연구_및_공학기술),
                    통신_방송_송출(JobFirst.연구_및_공학기술),
                    건설_채굴_연구_및_공학기술(JobFirst.연구_및_공학기술),
                    기계_로봇_금속_재료_연구_및_공학기술(JobFirst.연구_및_공학기술),
                    전기_전자_연구_및_공학기술(JobFirst.연구_및_공학기술),
                    화학_에너지_환경_연구_및_공학기술(JobFirst.연구_및_공학기술),
                    섬유_식품_소방_방재_산업안전_연구_및_공학기술(JobFirst.연구_및_공학기술),
                    제도사(JobFirst.연구_및_공학기술),
                    대학교수_학교_및_유치원_교사(JobFirst.교육_법률_사회복지_경찰_소방_및_군인),
                    외국어_문리_강사(JobFirst.교육_법률_사회복지_경찰_소방_및_군인),
                    정보통신_기술_기능계_강사(JobFirst.교육_법률_사회복지_경찰_소방_및_군인),
                    예능_학습지_기타_강사(JobFirst.교육_법률_사회복지_경찰_소방_및_군인),
                    장학관_교육조교(JobFirst.교육_법률_사회복지_경찰_소방_및_군인),
                    법률_전문가_및_법률_사무(JobFirst.교육_법률_사회복지_경찰_소방_및_군인),
                    사회복지_상담_직업상담_시민단체활동(JobFirst.교육_법률_사회복지_경찰_소방_및_군인),
                    보육교사_생활지도원_및_종교직(JobFirst.교육_법률_사회복지_경찰_소방_및_군인),
                    경찰_소방_교도_군인(JobFirst.교육_법률_사회복지_경찰_소방_및_군인),
                    의사_한의사_치과의사(JobFirst.보건_의료),
                    수의사(JobFirst.보건_의료),
                    의료기사_치료사_재활사(JobFirst.보건_의료),
                    그_외_보건_의료_종사자(JobFirst.보건_의료),
                    작가_통_번역_및_출판물_전문가(JobFirst.예술_디자인_방송_스포츠),
                    기자_및_언론_전문가(JobFirst.예술_디자인_방송_스포츠),
                    학예사_사서_기록물관리사(JobFirst.예술_디자인_방송_스포츠),
                    창작_공연(JobFirst.예술_디자인_방송_스포츠),
                    제품_패션_실내장식_시각_디자이너(JobFirst.예술_디자인_방송_스포츠),
                    미디어콘텐츠_UXUI_디자이너(JobFirst.예술_디자인_방송_스포츠),
                    연극_영화_방송(JobFirst.예술_디자인_방송_스포츠),
                    공연_음반_기획_및_매니저(JobFirst.예술_디자인_방송_스포츠),
                    스포츠_레크리에이션(JobFirst.예술_디자인_방송_스포츠),
                    미용_및_반려동물_서비스(JobFirst.미용_여행_숙박_음식_경비_돌봄_청소),
                    결혼_장례_등_예식_서비스(JobFirst.미용_여행_숙박_음식_경비_돌봄_청소),
                    여행_객실승무_숙박_오락_서비스(JobFirst.미용_여행_숙박_음식_경비_돌봄_청소),
                    주방장_및_조리사(JobFirst.미용_여행_숙박_음식_경비_돌봄_청소),
                    식당_서비스(JobFirst.미용_여행_숙박_음식_경비_돌봄_청소),
                    경호_보안(JobFirst.미용_여행_숙박_음식_경비_돌봄_청소),
                    경비원(JobFirst.미용_여행_숙박_음식_경비_돌봄_청소),
                    돌봄_서비스(JobFirst.미용_여행_숙박_음식_경비_돌봄_청소),
                    청소_방역_및_가사_서비스(JobFirst.미용_여행_숙박_음식_경비_돌봄_청소),
                    검침_주차관리_및_기타_단순_서비스(JobFirst.미용_여행_숙박_음식_경비_돌봄_청소),
                    부동산중개(JobFirst.영업_판매_운전_운송),
                    기술_의약품_해외_영업_및_상품_중개(JobFirst.영업_판매_운전_운송),
                    자동차_제품_기타_영업(JobFirst.영업_판매_운전_운송),
                    텔레마케터(JobFirst.영업_판매_운전_운송),
                    소규모_판매점장_및_상점_판매(JobFirst.영업_판매_운전_운송),
                    통신서비스_온라인판매_상품대여_노점_이동판매_및_주유(JobFirst.영업_판매_운전_운송),
                    매장_계산_및_매표(JobFirst.영업_판매_운전_운송),
                    판촉_및_기타_판매_종사자(JobFirst.영업_판매_운전_운송),
                    항공기_선박_철도_조종_및_관제(JobFirst.영업_판매_운전_운송),
                    자동차_운전(JobFirst.영업_판매_운전_운송),
                    물품이동장비_조작(JobFirst.영업_판매_운전_운송),
                    택배_납품영업_선박갑판_하역_및_기타_운송(JobFirst.영업_판매_운전_운송),
                    건설구조_기능(JobFirst.건설_채굴),
                    건축마감_기능(JobFirst.건설_채굴),
                    배관(JobFirst.건설_채굴),
                    건설_채굴_기계_운전(JobFirst.건설_채굴),
                    기타_건설_기능(JobFirst.건설_채굴),
                    건설_채굴_단순_종사자(JobFirst.건설_채굴),
                    기계장비_설치_정비(JobFirst.설치_정비_생산_기계_금속_재료),
                    운송장비_정비(JobFirst.설치_정비_생산_기계_금속_재료),
                    금형_및_공작기계_조작(JobFirst.설치_정비_생산_기계_금속_재료),
                    냉난방_설비_자동_조립라인_산업용_로봇_조작(JobFirst.설치_정비_생산_기계_금속_재료),
                    기계_및_운송장비_조립(JobFirst.설치_정비_생산_기계_금속_재료),
                    금속관련_기계_설비_조작(JobFirst.설치_정비_생산_기계_금속_재료),
                    판금_제관_단조_주조(JobFirst.설치_정비_생산_기계_금속_재료),
                    용접(JobFirst.설치_정비_생산_기계_금속_재료),
                    도장_도금(JobFirst.설치_정비_생산_기계_금속_재료),
                    비금속제품_생산기계_조작(JobFirst.설치_정비_생산_기계_금속_재료),
                    전기공(JobFirst.설치_정비_생산_전기_전자_정보통신),
                    전기_전자_기기_설치_수리(JobFirst.설치_정비_생산_전기_전자_정보통신),
                    발전_배전_장비_전기_전자_설비_조작(JobFirst.설치_정비_생산_전기_전자_정보통신),
                    전기_전자_부품_제품_생산기계_조작(JobFirst.설치_정비_생산_전기_전자_정보통신),
                    전기_전자_부품_제품_조립(JobFirst.설치_정비_생산_전기_전자_정보통신),
                    정보통신기기_설치_수리(JobFirst.설치_정비_생산_전기_전자_정보통신),
                    방송_통신장비_및_케이블_설치_수리(JobFirst.설치_정비_생산_전기_전자_정보통신),
                    석유_화학물_가공장치_조작(JobFirst.설치_정비_생산_화학_환경_섬유_의복_식품가공),
                    고무_플라스틱_및_화학제품_생산기계_조작(JobFirst.설치_정비_생산_화학_환경_섬유_의복_식품가공),
                    환경_장치_조작(JobFirst.설치_정비_생산_화학_환경_섬유_의복_식품가공),
                    섬유_제조_및_가공_기계_조작(JobFirst.설치_정비_생산_화학_환경_섬유_의복_식품가공),
                    패턴_재단_재봉(JobFirst.설치_정비_생산_화학_환경_섬유_의복_식품가공),
                    의복_제조_및_수선(JobFirst.설치_정비_생산_화학_환경_섬유_의복_식품가공),
                    제화_기타_섬유_의복_기계_조작(JobFirst.설치_정비_생산_화학_환경_섬유_의복_식품가공),
                    제과_제빵_및_떡_제조(JobFirst.설치_정비_생산_화학_환경_섬유_의복_식품가공),
                    식품가공_기능원(JobFirst.설치_정비_생산_화학_환경_섬유_의복_식품가공),
                    식품가공_기계_조작(JobFirst.설치_정비_생산_화학_환경_섬유_의복_식품가공),
                    인쇄기계_사진현상기_조작(JobFirst.설치_정비_생산_인쇄_목재_공예_및_제조_단순),
                    목재_펄프_종이_생산(JobFirst.설치_정비_생산_인쇄_목재_공예_및_제조_단순),
                    가구_목제품_제조_수리(JobFirst.설치_정비_생산_인쇄_목재_공예_및_제조_단순),
                    공예_및_귀금속_세공(JobFirst.설치_정비_생산_인쇄_목재_공예_및_제조_단순),
                    악기_간판_및_기타_제조(JobFirst.설치_정비_생산_인쇄_목재_공예_및_제조_단순),
                    제조_단순_종사자(JobFirst.설치_정비_생산_인쇄_목재_공예_및_제조_단순),
                    작물재배(JobFirst.농림어업직),
                    낙농_사육(JobFirst.농림어업직),
                    임업_종사자(JobFirst.농림어업직),
                    어업_종사자(JobFirst.농림어업직),
                    농림어업_단순_종사자(JobFirst.농림어업직)
            
            ### 직무 소분류 (JobThird):
            REACT, VUE, ANGULAR, NODEJS, JAVA, PYTHON, SPRING, DJANGO,
            ANDROID, IOS, FLUTTER, AWS, DOCKER, KUBERNETES, SQL, NOSQL,
            FIGMA, PHOTOSHOP, ILLUSTRATOR, PREMIERE, AFTER_EFFECTS
            
            ## 대화 가이드라인
            
            1. **자연스러운 대화**: 딱딱하지 않게, 친구처럼 편안하게 대화하세요
            2. **한 번에 하나씩**: 지역 → 직무 순서로 하나씩 물어보세요
            3. **경청과 공감**: 사용자 답변에 공감하고 긍정적으로 반응하세요
            4. **유연한 파악**: 정확한 용어가 아니어도 의도를 파악하여 매칭하세요
               - 예: "서울이나 경기" → SEOUL, GYEONGGI
               - 예: "웹 개발자" → DEVELOPMENT, FRONTEND/BACKEND, REACT/NODEJS
            5. **추가 질문**: 모호한 경우 "백엔드와 프론트엔드 중 어느 쪽에 관심이 있으신가요?" 같이 물어보세요
            
            ## 완료 조건
            
            다음 정보가 모두 파악되면 대화를 마무리하세요:
            - ✅ 희망 지역 1개 이상
            - ✅ JobFirst 1개 이상
            - ✅ JobSecond 1개 이상  
            - ✅ JobThird 1개 이상
            
            ## 완료 시 응답 형식
            
            모든 정보가 파악되면 다음과 같이 요약하고 **반드시 특수 태그를 포함**하세요:
            
            ```
            완벽합니다! 회원님의 정보를 정리해드릴게요 😊
            
            📍 희망 근무 지역: 서울, 경기
            💼 희망 직무: 웹 개발 > 프론트엔드 > React
            
            이 정보를 바탕으로 맞춤 채용 공고를 추천해드리겠습니다!
            곧 메인 화면으로 이동합니다. 감사합니다! 🎉
            
            [ONBOARDING_COMPLETE]
            [AREAS:SEOUL,GYEONGGI]
            [JOB_FIRST:DEVELOPMENT]
            [JOB_SECOND:FRONTEND]
            [JOB_THIRD:REACT]
            ```
            
            ## 주의사항
            
            - 대화 히스토리를 참고하여 이미 물어본 내용은 반복하지 마세요
            - 사용자가 이미 언급한 정보를 기억하고 활용하세요
            - 완료 조건이 충족되지 않았다면 부드럽게 추가 정보를 요청하세요
            - 특수 태그([...])는 시스템용이므로 응답 맨 끝에 포함하세요
            """;
    }

    private OnboardingResult extractResultFromResponse(String response) {
        Set<Area> areas = new HashSet<>();
        Set<JobFirst> jobFirsts = new HashSet<>();
        Set<JobSecond> jobSeconds = new HashSet<>();
        Set<JobThird> jobThirds = new HashSet<>();

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

        // [JOB_THIRD:...] 파싱
        Pattern jobThirdPattern = Pattern.compile("\\[JOB_THIRD:([^\\]]+)\\]");
        Matcher jobThirdMatcher = jobThirdPattern.matcher(response);
        if (jobThirdMatcher.find()) {
            String[] names = jobThirdMatcher.group(1).split(",");
            for (String name : names) {
                try {
                    jobThirds.add(JobThird.valueOf(name.trim()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid JobThird: {}", name);
                }
            }
        }

        return OnboardingResult.builder()
                .recommendedAreas(areas)
                .recommendedJobFirsts(jobFirsts)
                .recommendedJobSeconds(jobSeconds)
                .recommendedJobThirds(jobThirds)
                .build();
    }

    private String cleanResponse(String response) {
        // 특수 태그 제거
        return response
                .replaceAll("\\[ONBOARDING_COMPLETE\\]", "")
                .replaceAll("\\[AREAS:[^\\]]+\\]", "")
                .replaceAll("\\[JOB_FIRST:[^\\]]+\\]", "")
                .replaceAll("\\[JOB_SECOND:[^\\]]+\\]", "")
                .replaceAll("\\[JOB_THIRD:[^\\]]+\\]", "")
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
        jobSeeker.setJobThirds(result.recommendedJobThirds());

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