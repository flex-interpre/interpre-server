package com.flex.interpre.domain.user.entity;

import com.flex.interpre.domain.document.entity.Document;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.interview.entity.Interview;
import com.flex.interpre.domain.user.dto.request.UpdateMyJobSeekerInfo;
import com.flex.interpre.global.constant.Area;
import com.flex.interpre.global.constant.JobFirst;
import com.flex.interpre.global.constant.JobSecond;
import com.flex.interpre.global.constant.JobThird;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "job_seekers")
public class JobSeeker {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

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