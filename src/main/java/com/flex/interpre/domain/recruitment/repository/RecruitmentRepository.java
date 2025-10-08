package com.flex.interpre.domain.recruitment.repository;

import com.flex.interpre.domain.recruitment.entity.Recruitment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, UUID> {

    // 공고 목록 조회 시 활성화된 공고만 조회, 연관 테이블 함께 조회함
    @EntityGraph(attributePaths = {"company", "jobFirsts"})
    @Query(value = "SELECT r FROM Recruitment r WHERE r.active = true", countQuery = "SELECT COUNT(r) FROM Recruitment r WHERE r.active = true")
    Page<Recruitment> findAllActive(Pageable pageable);


}
