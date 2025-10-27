package com.flex.interpre.domain.onboarding.repository;

import com.flex.interpre.domain.onboarding.entity.OnboardingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnboardingSessionRepository extends JpaRepository<OnboardingSession, UUID> {
    Optional<OnboardingSession> findByUserIdAndCompletedTrue(UUID userId);
}