package com.flex.interpre.domain.jobSeeker.entity;

import com.flex.interpre.domain.document.entity.Document;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.interview.entity.Interview;
import com.flex.interpre.domain.jobSeeker.dto.UpdateMyJobSeekerInfo;
import com.flex.interpre.global.constant.*;
import com.flex.interpre.global.security.authentication.AccountPrincipal;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "job_seekers", indexes = {
        @Index(name = "idx_job_seeker_email", columnList = "email"),
        @Index(name = "idx_job_seeker_google_id", columnList = "google_id")
})
public class JobSeeker implements AccountPrincipal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    // 공통 필드

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "google_id", length = 255, unique = true) // 다른 소셜 로그인 고려 nullable
    private String googleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private boolean approved = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // 구직자 필드

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Education education;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_seeker_desired_areas", joinColumns = @JoinColumn(name = "job_seeker_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "desired_area")
    @Builder.Default
    private Set<Area> desiredAreas = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_seeker_job_firsts", joinColumns = @JoinColumn(name = "job_seeker_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "job_first")
    @Builder.Default
    private Set<JobFirst> jobFirsts = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_seeker_job_seconds", joinColumns = @JoinColumn(name = "job_seeker_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "job_second")
    @Builder.Default
    private Set<JobSecond> jobSeconds = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_seeker_job_thirds", joinColumns = @JoinColumn(name = "job_seeker_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "job_third")
    @Builder.Default
    private Set<JobThird> jobThirds = new HashSet<>();

    @OneToMany(mappedBy = "jobSeeker", fetch = FetchType.LAZY)
    List<Interview> interviews;
    @Builder.Default
    @OneToMany(mappedBy = "jobSeeker", fetch = FetchType.LAZY)
    private Set<Document> documents = new HashSet<>();


    // 구직자 메서드

    // 인증용 role 반환
    public Role getRole() {
        return Role.JOB_SEEKER;
    }

    // 구직자 정보 수정 메서드
    public void update(UpdateMyJobSeekerInfo request) {
        this.name = request.name();
        this.education = request.education();
        this.desiredAreas = request.desiredAreas();
        this.jobFirsts = request.jobFirsts();
        this.jobSeconds = request.jobSeconds();
        this.jobThirds = request.jobThirds();
    }

    @ManyToMany
    @JoinTable(
            name = "job_seeker_bookmarks",
            joinColumns = @JoinColumn(name = "job_seeker_id"),
            inverseJoinColumns = @JoinColumn(name = "recruitment_id")
    )
    @Builder.Default
    private Set<Recruitment> bookmarkedRecruitments = new HashSet<>();

}