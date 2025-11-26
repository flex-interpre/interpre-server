package com.flex.interpre.domain.jobSeeker.repository;

import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobSeekerRepository extends JpaRepository<JobSeeker, UUID> {
    Optional<JobSeeker> findByEmail(String email);
    Optional<JobSeeker> findByGoogleId(String googleId);
    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT js FROM JobSeeker js " +
           "LEFT JOIN FETCH js.interviews " +
           "LEFT JOIN FETCH js.desiredAreas " +
           "LEFT JOIN FETCH js.jobFirsts " +
           "LEFT JOIN FETCH js.jobSeconds " +
           "LEFT JOIN FETCH js.jobThirds " +
           "WHERE js.id = :id")
    JobSeeker findByIdWithInterviews(UUID id);

//    @Query("SELECT j FROM JobSeeker j JOIN FETCH j.user u WHERE j.user.id = :id")
//    Optional<JobSeeker> findByUserIdWithUser(UUID id);
//
//    @Query("SELECT js FROM JobSeeker js LEFT JOIN FETCH js.interviews WHERE js.user.id = :id")
//    JobSeeker findByUserIdWithInterviews(UUID id);
//
//    @Query("SELECT js FROM JobSeeker js LEFT JOIN FETCH js.bookmarkedRecruitments WHERE js.id = :id")
//    Optional<JobSeeker> findByIdWithBookmarks(UUID id);
}
