package com.flex.interpre.domain.interview.dto.response;

import com.flex.interpre.domain.interview.entity.Competency;
import com.flex.interpre.domain.interview.entity.InterviewReport;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;

@Builder
public record InterviewReportDto(
        UUID id,
        LocalDateTime createdAt,
        String aiFeedback,
        List<String> weaknesses,
        List<String> strengths,
        Map<Competency, Integer> competencyScores
) {
    public static InterviewReportDto from(InterviewReport report) {
        return InterviewReportDto.builder()
                .id(report.getId())
                .createdAt(report.getCreatedAt())
                .aiFeedback(report.getAiFeedback())
                .weaknesses(report.getWeaknesses())
                .strengths(report.getStrengths())
                .competencyScores(report.getCompetencyScores())
                .build();
    }
}
