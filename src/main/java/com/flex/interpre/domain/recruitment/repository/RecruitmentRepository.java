package com.flex.interpre.domain.recruitment.repository;

import com.flex.interpre.domain.recruitment.entity.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, UUID> {
}
