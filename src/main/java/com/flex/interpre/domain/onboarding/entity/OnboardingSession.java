package com.flex.interpre.domain.onboarding.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "onboarding_sessions")
public class OnboardingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "job_seeker_id", nullable = false, columnDefinition = "uuid")
    private UUID jobSeekerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false, length = 50)
    private OnboardingStep currentStep;

    @Column(name = "conversation_history", columnDefinition = "TEXT")
    private String conversationHistory;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private boolean completed = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum OnboardingStep {
        AREA,
        JOB_FIRST,
        JOB_SECOND,
        COMPLETED
    }
}