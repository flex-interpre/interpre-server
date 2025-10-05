package com.flex.interpre.domain.recruitment.dto.response;

import com.flex.interpre.domain.recruitment.entity.*;
import jakarta.annotation.Nonnull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Builder
public record RecruitmentResponse(
        UUID id,

        UUID companyId,
        String companyName,
        String logoUrl,

        String title,
        Set<JobGroup> jobGroups,
        Set<Job> jobs,
        Set<EmploymentType> employmentTypes,
        Integer minExperience,
        Integer maxExperience,
        String location,
        String description,
        Set<String> requirements,
        Set<String> benefits,
        Set<String> skills,
        LocalDateTime deadline,
        boolean active,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public static RecruitmentResponse from(Recruitment recruitment) {
        return RecruitmentResponse.builder()
                .id(recruitment.getId())
                .companyId(recruitment.getCompany().getId())
                .companyName(recruitment.getCompany().getCompanyName())
                .logoUrl(recruitment.getCompany().getLogoUrl())
                .title(recruitment.getTitle())
                .jobGroups(recruitment.getJobGroups())
                .jobs(recruitment.getJobs())
                .employmentTypes(recruitment.getEmploymentTypes())
                .minExperience(recruitment.getMinExperience())
                .maxExperience(recruitment.getMaxExperience())
                .location(recruitment.getLocation())
                .description(recruitment.getDescription())
                .requirements(recruitment.getRequirements())
                .benefits(recruitment.getBenefits())
                .skills(recruitment.getSkills())
                .deadline(recruitment.getDeadline())
                .active(recruitment.isActive())
                .viewCount(recruitment.getViewCount())
                .createdAt(recruitment.getCreatedAt())
                .updatedAt(recruitment.getUpdatedAt())
                .build();
    }
}
