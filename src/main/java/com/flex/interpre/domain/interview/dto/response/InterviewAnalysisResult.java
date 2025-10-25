package com.flex.interpre.domain.interview.dto.response;


import com.flex.interpre.domain.interview.entity.Competency;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record InterviewAnalysisResult(
        List<String> strengths,
        List<String> weaknesses,
        String aiFeedback,
        Map<Competency, Integer> competencyScores
) {
}
