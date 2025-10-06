package com.flex.interpre.domain.recruitment.dto.response;

import com.flex.interpre.global.constant.JobFirst;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Builder
public record RecruitmentSummaryResponse(
        UUID id,
        CompanySummaryResponse company,
        String title,
        Set<JobFirst> jobFirsts,
        LocalDateTime deadline,
        int viewCount
){
    public static RecruitmentSummaryResponse from(Recruitment recruitment) {
        return RecruitmentSummaryResponse.builder()
                .id(recruitment.getId())
                .company(CompanySummaryResponse.from(recruitment.getCompany()))
                .title(recruitment.getTitle())
                .jobFirsts(recruitment.getJobFirsts())
                .deadline(recruitment.getDeadline())
                .viewCount(recruitment.getViewCount())
                .build();
    }
}