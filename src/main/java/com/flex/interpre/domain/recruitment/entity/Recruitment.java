package com.flex.interpre.domain.recruitment.entity;

import com.flex.interpre.domain.recruitment.dto.request.RecruitmentCreateUpdateRequest;
import com.flex.interpre.domain.company.entity.Company;
import com.flex.interpre.global.constant.Area;
import com.flex.interpre.global.constant.JobSecond;
import com.flex.interpre.global.constant.JobFirst;
import com.flex.interpre.global.constant.JobThird;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@NamedEntityGraph(
        name = "Recruitment.withDetails",
        attributeNodes = {
                @NamedAttributeNode("company"),
                @NamedAttributeNode("jobAreas"),
                @NamedAttributeNode("jobFirsts"),
                @NamedAttributeNode("jobSeconds"),
                @NamedAttributeNode("jobThirds"),
                @NamedAttributeNode("employmentTypes"),
                @NamedAttributeNode("requirements"),
                @NamedAttributeNode("benefits"),
                @NamedAttributeNode("skills")
        }
)
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

    @Column(nullable = false, length = 200)
    private String title;

    @Lob @Column(nullable = false)
    String description;

    @Column(name ="deadline")
    LocalDateTime deadline;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_areas", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "job_area", nullable = false) @Builder.Default
    private Set<Area> jobAreas = new HashSet<>();

    @Column(length = 200)
    String location;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_firsts", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "job_first", nullable = false) @Builder.Default
    private Set<JobFirst> jobFirsts = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_seconds", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "job_second", nullable = false) @Builder.Default
    private Set<JobSecond> jobSeconds = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_thirds", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "job_third", nullable = false) @Builder.Default
    private Set<JobThird> jobThirds = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "employment_types", joinColumns = @JoinColumn(name = "recruitment_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20) @Builder.Default
    private Set<EmploymentType> employmentTypes = new HashSet<>();

    @Column(name ="min_experience")
    Integer minExperience;

    @Column(name ="max_experience")
    Integer maxExperience;

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


    @Column(name ="is_active") @Builder.Default
    boolean active = true;

    @Column(name ="view_count") @Builder.Default
    int viewCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 조회수 증가 메서드
    public void increaseViewCount() {
        this.viewCount++;
    }

    // 공고문 비활성화 메서드
    public void deactivate() {
        this.active = false;
    }

    // 공고문 생성
    public static Recruitment create(RecruitmentCreateUpdateRequest request, Company company) {
        return Recruitment.builder()
                .company(company)
                .title(request.title())
                .description(request.description())
                .deadline(request.deadline())
                .jobAreas(request.jobAreas())
                .location(request.location())
                .jobFirsts(request.jobFirsts())
                .jobSeconds(request.jobSeconds())
                .jobThirds(request.jobThirds())
                .employmentTypes(request.employmentTypes())
                .minExperience(request.minExperience())
                .maxExperience(request.maxExperience())
                .requirements(request.requirements())
                .benefits(request.benefits())
                .skills(request.skills())
                .active(true)
                .viewCount(0)
                .build();
    }

    // 공고문 수정
    public void update(RecruitmentCreateUpdateRequest request) {
        this.title = request.title();
        this.description = request.description();
        this.deadline = request.deadline();
        this.jobAreas = request.jobAreas();
        this.location = request.location();
        this.jobFirsts = request.jobFirsts();
        this.jobSeconds = request.jobSeconds();
        this.jobThirds = request.jobThirds();
        this.employmentTypes = request.employmentTypes();
        this.minExperience = request.minExperience();
        this.maxExperience = request.maxExperience();
        this.requirements = request.requirements();
        this.benefits = request.benefits();
        this.skills = request.skills();
    }
}