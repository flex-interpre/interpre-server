package com.flex.interpre.domain.interview.repository;

import com.flex.interpre.domain.interview.entity.InterviewReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InterviewReportRepository extends JpaRepository<InterviewReport, UUID> {
}
