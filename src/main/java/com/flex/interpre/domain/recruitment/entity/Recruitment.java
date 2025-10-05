package com.flex.interpre.domain.recruitment.entity;

import com.flex.interpre.domain.recruitment.dto.request.RecruitmentRequest;
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
@Getter @Builder
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = AccessLevel.PROTECTED) @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name= "recruitments")
public class Recruitment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
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
    @Column(name = "job_group", nullable = false) @Builder.Default
    private Set<JobGroup> jobGroups = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "jobs", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "job", nullable = false) @Builder.Default
    private Set<Job> jobs = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "employment_types", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20) @Builder.Default
    private Set<EmploymentType> employmentTypes = new HashSet<>();


    @Column(name ="min_experience")
    Integer minExperience;

    @Column(name ="max_experience")
    Integer maxExperience;

    @Column(nullable = false, length = 200)
    String location;

    @Lob
    @Column(nullable = false)
    String description;

    @ElementCollection
    @CollectionTable(name = "recruitment_requirements", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Column(name = "requirement") @Builder.Default
    private Set<String> requirements = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "recruitment_benefits", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Column(name = "benefit") @Builder.Default
    private Set<String> benefits = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "recruitment_skills", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Column(name = "skill") @Builder.Default
    private Set<String> skills = new HashSet<>();

    @Column(name ="deadline")
    LocalDateTime deadline;

    @Column(name ="is_active") @Builder.Default
    boolean active = true;

    @Column(name ="view_count") @Builder.Default
    int viewCount = 0;

    // 조회수 증가 메서드
    public void increaseViewCount() {
        this.viewCount++;
    }

    // 공고문 비활성화 메서드
    public void deactivate() {
        this.active = false;
    }

    // 공고문 생성
    public static Recruitment create(RecruitmentRequest request, Company company) {
        return Recruitment.builder()
                .company(company)
                .title(request.title())
                .jobGroups(request.jobGroups())
                .jobs(request.jobs())
                .employmentTypes(request.employmentTypes())
                .minExperience(request.minExperience())
                .maxExperience(request.maxExperience())
                .location(request.location())
                .description(request.description())
                .requirements(request.requirements())
                .benefits(request.benefits())
                .skills(request.skills())
                .deadline(request.deadline())
                .build();
    }

    // 공고문 수정
    public void update(RecruitmentRequest request) {
        this.title = request.title();
        this.jobGroups = request.jobGroups();
        this.jobs = request.jobs();
        this.employmentTypes = request.employmentTypes();
        this.minExperience = request.minExperience();
        this.maxExperience = request.maxExperience();
        this.location = request.location();
        this.description = request.description();
        this.requirements = request.requirements();
        this.benefits = request.benefits();
        this.skills = request.skills();
        this.deadline = request.deadline();
    }
}