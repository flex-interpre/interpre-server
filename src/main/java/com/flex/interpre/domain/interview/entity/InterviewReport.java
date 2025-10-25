package com.flex.interpre.domain.interview.entity;

import com.flex.interpre.domain.recruitment.entity.Recruitment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EntityListeners({AuditingEntityListener.class})
public class InterviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id")
    private Interview interview;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ai_feedback")
    private String aiFeedback;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Recruitment> recommendations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "interview_report_weaknesses",
            joinColumns = @JoinColumn(name = "report_id")
    )
    @Column(name = "weakness", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> weaknesses = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "interview_report_strengths",
            joinColumns = @JoinColumn(name = "report_id")
    )
    @Column(name = "strength", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> strengths = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "interview_report_competency_scores",
            joinColumns = @JoinColumn(name = "report_id")
    )

    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "competency")
    @Column(name = "score")
    @Builder.Default
    private Map<Competency, Integer> competencyScores = new HashMap<>();
}
