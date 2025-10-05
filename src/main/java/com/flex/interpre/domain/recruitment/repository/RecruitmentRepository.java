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

    // 기업 엔티티와 공고문 페치 조인
    @Query("SELECT r FROM Recruitment r JOIN FETCH r.company WHERE r.id = :id")
    Optional<Recruitment> findByIdWithCompany(@Param("id") UUID id);

    @Query("SELECT DISTINCT r FROM Recruitment r JOIN FETCH r.company")
    Page<Recruitment> findAllWithCompany(Pageable pageable);
}
