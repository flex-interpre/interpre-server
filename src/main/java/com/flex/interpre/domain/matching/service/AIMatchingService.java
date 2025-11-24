package com.flex.interpre.domain.matching.service;

import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.jobSeeker.repository.BookmarkRepository;
import com.flex.interpre.domain.jobSeeker.repository.RecommendationFeedbackRepository;
import com.flex.interpre.domain.matching.MatchingScoreCalculator;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.recruitment.service.RecruitmentIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIMatchingService {
    private final RecruitmentIndexService recruitmentIndexService;
    private final MatchingScoreCalculator scoreCalculator;
    private final BookmarkRepository bookmarkRepository;
    private final RecommendationFeedbackRepository feedbackRepository;

    private static final int DEFAULT_K = 40; // k 기본값 설정

    // // 인터뷰 벡터로 OpenSearch KNN -> 후보군 추출 (면접 종료 후 호출됨)
    public List<Recruitment> recommendForInterview(JobSeeker js, List<Double> interviewVector) throws IOException {
        List<Recruitment> candidates = recruitmentIndexService.searchByVector(interviewVector, DEFAULT_K);

        return rerank(js, candidates); // 리랭킹하여 반환
    }

    // 일반 추천 (프로필 벡터 기반)
    public List<Recruitment> recommend(JobSeeker js) throws IOException {
        List<Double> vec = js.getProfileEmbedding(); // 구직자 프로필 벡터
        if (vec == null || vec.isEmpty())   return Collections.emptyList();

        // 기존 구직자 프로필 벡터로 후보군 추출
        List<Recruitment> candidates = recruitmentIndexService.searchByVector(vec, DEFAULT_K);

        return rerank(js, candidates); // 리랭킹하여 반환
    }

    // 리랭킹
    private List<Recruitment> rerank(JobSeeker js, List<Recruitment> candidates) {
        // 북마크/피드백 ID
        Set<UUID> bookmarkIds = bookmarkRepository.findAllByJobSeekerIdAndLikedIsTrue(js.getId())
                .stream().map(b -> b.getRecruitment().getId()).collect(Collectors.toSet());

        Set<UUID> positiveFeedbackIds = feedbackRepository.findAllByJobSeekerId(js.getId())
                .stream().filter(f -> f.getScore() > 0) // 긍정적 평가만
                .map(f -> f.getRecruitment().getId()).collect(Collectors.toSet());

        // 가중치 기반 점수 계산
        return candidates.stream()
                .sorted((a, b) -> {
                    double s1 = scoreCalculator.calculateScore(js, a, bookmarkIds, positiveFeedbackIds);
                    double s2 = scoreCalculator.calculateScore(js, b, bookmarkIds, positiveFeedbackIds);
                    return Double.compare(s2, s1);
                })
                .limit(10).toList();
    }
}
