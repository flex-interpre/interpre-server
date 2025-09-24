package com.flex.interpre.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED) @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "job_seekers")
public class JobSeeker {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Education education;

    @ElementCollection
    @CollectionTable(name = "job_seeker_desired_areas",
            joinColumns = @JoinColumn(name = "job_seeker_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "desired_area")
    private List<Area> desiredAreas;

    @ElementCollection
    @CollectionTable(name = "job_seeker_desired_categories",
            joinColumns = @JoinColumn(name = "job_seeker_id"))
    @Column(name = "desired_job_category")
    private List<JobCategoty> desiredJobCategories;
}