package com.flex.interpre.domain.recruitment.dto.response;

import com.flex.interpre.domain.recruitment.entity.JobGroup;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record RecruitmentSummaryResponse(
        UUID id,
        CompanySummaryResponse company,
        String title,
        Set<JobGroup> jobGroups,
        LocalDateTime deadline,
        int viewCount
){
    public static RecruitmentSummaryResponse from(Recruitment recruitment) {
        return new RecruitmentSummaryResponse(
                recruitment.getId(),
                CompanySummaryResponse.from(recruitment.getCompany()),
                recruitment.getTitle(),
                recruitment.getJobGroups().stream().collect(Collectors.toSet()),
                recruitment.getDeadline(),
                recruitment.getViewCount()
        );
    }
}