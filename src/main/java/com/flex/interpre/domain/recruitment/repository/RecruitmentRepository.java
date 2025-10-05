package com.flex.interpre.domain.recruitment.repository;

import com.flex.interpre.domain.recruitment.entity.Recruitment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, UUID> {

    // 공고 목록 조회 시 활성화된 공고만 조회, 연관 테이블 페치 조인
    @Query(value = "SELECT DISTINCT r FROM Recruitment r JOIN FETCH r.company LEFT JOIN FETCH r.jobGroups WHERE r.active = true", countQuery = "SELECT COUNT(r) FROM Recruitment r WHERE r.active = true")
    Page<Recruitment> findAllActive(Pageable pageable);

    // 공고 상세 조회 시 연관 테이블 모두 페치 조인
    @Query("SELECT r FROM Recruitment r JOIN FETCH r.company LEFT JOIN FETCH r.jobGroups LEFT JOIN FETCH r.jobs LEFT JOIN FETCH r.employmentTypes LEFT JOIN FETCH r.requirements LEFT JOIN FETCH r.benefits LEFT JOIN FETCH r.skills WHERE r.id = :id")
    Optional<Recruitment> findByIdWithDetails(@Param("id") UUID id);
}
