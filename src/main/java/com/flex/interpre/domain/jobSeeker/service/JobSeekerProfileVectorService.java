package com.flex.interpre.domain.jobSeeker.service;


import com.flex.interpre.domain.jobSeeker.entity.Bookmark;
import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.jobSeeker.entity.RecommendationFeedback;
import com.flex.interpre.domain.jobSeeker.repository.BookmarkRepository;
import com.flex.interpre.domain.jobSeeker.repository.JobSeekerRepository;
import com.flex.interpre.domain.jobSeeker.repository.RecommendationFeedbackRepository;
import com.flex.interpre.domain.recruitment.service.RecruitmentIndexService;
import com.flex.interpre.global.module.embedding.ClovaEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSeekerProfileVectorService {
    private final JobSeekerRepository jobSeekerRepository;
    private final BookmarkRepository bookmarkRepository;
    private final RecommendationFeedbackRepository feedbackRepository;
    private final RecruitmentIndexService recruitmentIndexService;
    private final ClovaEmbeddingService clovaEmbeddingService;

    private static final int VECTOR_DIM = 1024; // 벡터 차원

    // 프로필 벡터 업데이트
    @Transactional
    public void updateProfileEmbedding(UUID jobSeekerId){
        JobSeeker jobSeeker = jobSeekerRepository.findByIdWithInterviews(jobSeekerId);

        List<Double> interviewVector = jobSeeker.getCumulativeEmbedding(); // 1 면접 누적 벡터

        // 2 Bookmark 벡터
        List<Bookmark> bookmarks = bookmarkRepository.findAllByJobSeekerIdAndLikedIsTrue(jobSeekerId);
        List<UUID> bookmarkedRecruitmentIds = bookmarks.stream().map(b -> b.getRecruitment().getId()).toList();
        List<Double> bookmarkVector = averageFromOpenSearch(bookmarkedRecruitmentIds); // 오픈서치에서 가져온 임베딩 평균 내어 할당

        // 3 매칭 피드백 기반 벡터
        List<RecommendationFeedback> feedbacks = feedbackRepository.findAllByJobSeekerId(jobSeekerId);
        List<UUID> feedbackRecruitmentIds = feedbacks.stream().map(f -> f.getRecruitment().getId()).toList();
        List<Double> feedbackVector = averageFromOpenSearch(feedbackRecruitmentIds);


        // 4 온보딩 기반 벡터 (텍스트 임베딩)
        String onboardingText = buildOnboardingText(jobSeeker);
        List<Double> onboardingVector = onboardingText.isBlank() ? emptyVector() : clovaEmbeddingService.embed(onboardingText);

        // 최종 결합 벡터
        List<List<Double>> vectors = List.of(
                interviewVector,
                bookmarkVector,
                feedbackVector,
                onboardingVector
        );
        List<Double> finalEmbedding = averageEmbeddings(vectors); // 1,2,3,4 벡터의 평균 할당

        jobSeeker.setProfileEmbedding(finalEmbedding); // 구직자 프로필 벡터로 db에 저장
        jobSeekerRepository.save(jobSeeker);

        log.info("[프로필 벡터 갱신] jobSeekerId={}, bookmark={}, feedback={}", jobSeekerId, bookmarkedRecruitmentIds.size(), feedbackRecruitmentIds.size());
    }


    // OpenSearch에서 벡터 평균
    private List<Double> averageFromOpenSearch(List<UUID> ids) {
        if (ids.isEmpty()) return emptyVector();

        Map<UUID, List<Double>> result = recruitmentIndexService.getEmbeddingsByIds(ids); // 임베딩 가져옴

        if (result.isEmpty()) return emptyVector();

        return averageEmbeddings(new ArrayList<>(result.values())); // 평균 계산해서 반환
    }

    // 여러 벡터 평균 계산
    private List<Double> averageEmbeddings(List<List<Double>> vectors) {
        // 유효 벡터인지 확인
        List<List<Double>> valid = vectors.stream().filter(v -> v != null && !v.isEmpty()).toList();

        if (valid.isEmpty()) return emptyVector();

        int dim = valid.get(0).size();

        return IntStream.range(0, dim)
                .mapToObj(i -> valid.stream()
                                .mapToDouble(v -> v.get(i))
                                .average()
                                .orElse(0.0)
                ).toList();
    }

    // 영벡터
    private List<Double> emptyVector() {
        return IntStream.range(0, VECTOR_DIM).mapToObj(i -> 0.0).toList();
    }

    // 온보딩 텍스트 구성
    private String buildOnboardingText(JobSeeker js) {
        StringBuilder sb = new StringBuilder();

        if (!js.getDesiredAreas().isEmpty()) sb.append("희망지역: ").append(js.getDesiredAreas()).append("\n");
        if (!js.getJobFirsts().isEmpty()) sb.append("희망직무1: ").append(js.getJobFirsts()).append("\n");
        if (!js.getJobSeconds().isEmpty()) sb.append("희망직무2: ").append(js.getJobSeconds()).append("\n");
        if (!js.getJobThirds().isEmpty()) sb.append("희망직무3: ").append(js.getJobThirds()).append("\n");

        return sb.toString();
    }
}