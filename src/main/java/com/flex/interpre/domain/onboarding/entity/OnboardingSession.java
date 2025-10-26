package com.flex.interpre.domain.onboarding.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "onboarding_sessions")
public class OnboardingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OnboardingStep currentStep;

    @Column(columnDefinition = "TEXT")
    private String conversationHistory;

    @Column(nullable = false)
    private Boolean completed;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    public enum OnboardingStep {
        GREETING,
        LOCATION_INQUIRY,
        JOB_TYPE_INQUIRY,
        CONFIRMATION,
        COMPLETED
    }
}