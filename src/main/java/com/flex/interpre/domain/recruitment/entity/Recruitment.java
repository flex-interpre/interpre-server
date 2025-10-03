package com.flex.interpre.domain.recruitment.entity;

import com.flex.interpre.domain.user.entity.Company;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recruitments")
public class Recruitment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false, length = 200)
    private String title;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_groups", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "job_group", nullable = false)
    @Builder.Default
    private Set<JobGroup> jobGroups = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "jobs", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "job", nullable = false)
    @Builder.Default
    private Set<Job> jobs = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "employment_types", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20)
    @Builder.Default
    private Set<EmploymentType> employmentTypes = new HashSet<>();


    @Column(name = "min_experience")
    Integer minExperience;

    @Column(name = "max_experience")
    Integer maxExperience;

    @Column(nullable = false, length = 200)
    String location;

    @Lob
    @Column(nullable = false)
    String description;

    @ElementCollection
    @CollectionTable(name = "recruitment_requirements", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Column(name = "requirement")
    @Builder.Default
    private Set<String> requirements = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "recruitment_benefits", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Column(name = "benefit")
    @Builder.Default
    private Set<String> benefits = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "recruitment_skills", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Column(name = "skill")
    @Builder.Default
    private Set<String> skills = new HashSet<>();

    @Column(name = "deadline")
    LocalDateTime deadline;

    @Column(name = "is_active")
    @Builder.Default
    boolean active = true;

    @Column(name = "view_count")
    @Builder.Default
    int viewCount = 0;
}