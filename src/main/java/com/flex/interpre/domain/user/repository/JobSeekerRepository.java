package com.flex.interpre.domain.user.repository;

import com.flex.interpre.domain.user.entity.JobSeeker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JobSeekerRepository extends JpaRepository<JobSeeker, UUID> {
    @Query("SELECT j FROM JobSeeker j JOIN FETCH j.user u WHERE j.id = :id")
    Optional<JobSeeker> findByIdWithUser(UUID id);
}
