package com.flex.interpre.domain.jobSeeker.repository;

import com.flex.interpre.domain.jobSeeker.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {
    Optional<Bookmark> findByJobSeekerIdAndRecruitmentId(UUID jobSeekerId, UUID recruitmentId);
    List<Bookmark> findAllByJobSeekerId(UUID jobSeekerId);
    List<Bookmark> findAllByJobSeekerIdAndLikedIsTrue(UUID jobSeekerId);
}
