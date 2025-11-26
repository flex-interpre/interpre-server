package com.flex.interpre.domain.jobSeeker.service;

import com.flex.interpre.domain.jobSeeker.dto.RecommendationFeedbackRequest;
import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.jobSeeker.entity.RecommendationFeedback;
import com.flex.interpre.domain.jobSeeker.repository.RecommendationFeedbackRepository;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.recruitment.exception.RecruitmentExceptions;
import com.flex.interpre.domain.recruitment.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationFeedbackService {
    private final RecommendationFeedbackRepository feedbackRepository;
    private final RecruitmentRepository recruitmentRepository;

    public void submitFeedback(JobSeeker jobSeeker, UUID recruitmentId, RecommendationFeedbackRequest request) {
        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
                .orElseThrow(RecruitmentExceptions.RECRUITMENT_NOT_FOUND::toException);

        RecommendationFeedback feedback = feedbackRepository.findByJobSeekerIdAndRecruitmentId(jobSeeker.getId(), recruitmentId)
                .orElseGet(() -> RecommendationFeedback.builder()
                        .jobSeeker(jobSeeker)
                        .recruitment(recruitment)
                        .build()
                );

        feedback.setScore(request.score());
        feedbackRepository.save(feedback);
    }
}