package com.flex.interpre.domain.jobSeeker.entity;

import com.flex.interpre.domain.recruitment.entity.Recruitment;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "recommendation_feedback", uniqueConstraints = @UniqueConstraint(columnNames = {"job_seeker_id", "recruitment_id"}))
public class RecommendationFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    private JobSeeker jobSeeker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id", nullable = false)
    private Recruitment recruitment;

    @Column(nullable = false)
    private Integer score; // +1 잘 맞았음, 0 보통, -1 안 맞음

    @CreatedDate
    private LocalDateTime createdAt;
}
