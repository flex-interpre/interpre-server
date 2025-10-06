package com.flex.interpre.domain.recruitment.dto.response;

import com.flex.interpre.domain.recruitment.entity.*;
import com.flex.interpre.global.constant.Area;
import com.flex.interpre.global.constant.JobSecond;
import com.flex.interpre.global.constant.JobFirst;
import com.flex.interpre.global.constant.JobThird;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Builder
public record RecruitmentResponse(
        UUID id,
        CompanySummaryResponse company,
        String title,
        String description,
        LocalDateTime deadline,

        Set<Area> jobAreas,
        String location,

        Set<JobFirst> jobFirsts,
        Set<JobSecond> jobSeconds,
        Set<JobThird> jobThirds,
        Set<EmploymentType> employmentTypes,

        Integer minExperience,
        Integer maxExperience,

        Set<String> requirements,
        Set<String> benefits,
        Set<String> skills,

        boolean active,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public static RecruitmentResponse from(Recruitment recruitment) {
        return RecruitmentResponse.builder()
                .id(recruitment.getId())
                .company(CompanySummaryResponse.from(recruitment.getCompany()))
                .title(recruitment.getTitle())
                .description(recruitment.getDescription())
                .deadline(recruitment.getDeadline())

                .jobAreas(recruitment.getJobAreas())
                .location(recruitment.getLocation())

                .jobFirsts(recruitment.getJobFirsts())
                .jobSeconds(recruitment.getJobSeconds())
                .jobThirds(recruitment.getJobThirds())
                .employmentTypes(recruitment.getEmploymentTypes())

                .minExperience(recruitment.getMinExperience())
                .maxExperience(recruitment.getMaxExperience())

                .requirements(recruitment.getRequirements())
                .benefits(recruitment.getBenefits())
                .skills(recruitment.getSkills())

                .active(recruitment.isActive())
                .viewCount(recruitment.getViewCount())
                .createdAt(recruitment.getCreatedAt())
                .updatedAt(recruitment.getUpdatedAt())
                .build();
    }
}
