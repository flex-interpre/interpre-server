package com.flex.interpre.domain.matching;

import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
@RequiredArgsConstructor
public class MatchingScoreCalculator {

    // 요소별 점수 정의
    public double calculateScore(JobSeeker js, Recruitment rec, Set<UUID> bookmarkedIds, Set<UUID> positiveFeedbackIds){
        double score = 1.0; // 기본점수

        // 1) 온보딩 가중치
        // 희망 근무지역 점수
        if (!Collections.disjoint(js.getDesiredAreas(), rec.getJobAreas()))
            score += 0.4;

        // 희망 직무 분야 점수
        boolean matchesJob = !Collections.disjoint(js.getJobFirsts(), rec.getJobFirsts()) || !Collections.disjoint(js.getJobSeconds(), rec.getJobSeconds()) || !Collections.disjoint(js.getJobThirds(), rec.getJobThirds());
        if (matchesJob)
            score += 0.6;

        // 2) 북마크 가중치
        if (bookmarkedIds.contains(rec.getId()))
            score += 1.0;

        // 3) 공고 추천 피드백 기반 가중치
        if (positiveFeedbackIds.contains(rec.getId()))
            score += 1.2;

        return score;
    }
}
