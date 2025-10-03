package com.flex.interpre.domain.user.entity;

import com.flex.interpre.domain.bookmark.entity.BookMark;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter @Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED) @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "job_seekers")
public class JobSeeker {
    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
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

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_seeker_desired_areas",
            joinColumns = @JoinColumn(name = "job_seeker_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "desired_area")
    @Builder.Default
    private Set<Area> desiredAreas = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_seeker_desired_categories",
            joinColumns = @JoinColumn(name = "job_seeker_id"))
    @Column(name = "desired_job_category")
    @Builder.Default
    private Set<JobCategory> desiredJobCategories = new HashSet<>();

    @OneToMany(mappedBy = "jobSeeker", fetch = FetchType.LAZY)
    @Builder.Default
    @Column(name = "bookmarks")
    private Set<BookMark> bookmarks = new HashSet<>();
}