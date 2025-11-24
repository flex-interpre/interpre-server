package com.flex.interpre.domain.jobSeeker.repository;

import com.flex.interpre.domain.jobSeeker.entity.RecommendationFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, UUID> {
    Optional<RecommendationFeedback> findByJobSeekerIdAndRecruitmentId(UUID jobSeekerId, UUID recruitmentId);
}
