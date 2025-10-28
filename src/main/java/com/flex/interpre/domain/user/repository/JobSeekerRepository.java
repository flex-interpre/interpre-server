package com.flex.interpre.domain.user.repository;

import com.flex.interpre.domain.user.entity.JobSeeker;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobSeekerRepository extends JpaRepository<JobSeeker, UUID> {
    @Query("SELECT j FROM JobSeeker j JOIN FETCH j.user u WHERE j.user.id = :id")
    Optional<JobSeeker> findByUserIdWithUser(UUID id);

    @Query("SELECT js FROM JobSeeker js LEFT JOIN FETCH js.interviews WHERE js.user.id = :id")
    JobSeeker findByUserIdWithInterviews(UUID id);

    @Query("SELECT js FROM JobSeeker js LEFT JOIN FETCH js.bookmarkedRecruitments WHERE js.id = :id")
    Optional<JobSeeker> findByIdWithBookmarks(UUID id);
}
