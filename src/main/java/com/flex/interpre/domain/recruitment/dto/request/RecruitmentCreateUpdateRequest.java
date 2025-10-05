package com.flex.interpre.domain.recruitment.dto.request;

import com.flex.interpre.domain.recruitment.entity.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

public record RecruitmentCreateUpdateRequest(
        @NotBlank String title,
        @NotEmpty Set<JobGroup> jobGroups,
        @NotEmpty Set<Job> jobs,
        @NotEmpty Set<EmploymentType> employmentTypes,
        Integer minExperience,
        Integer maxExperience,
        @NotBlank String location,
        @NotBlank String description,
        @NotEmpty Set<String> requirements,
        @NotEmpty Set<String> benefits,
        @NotEmpty Set<String> skills,
        @Future LocalDateTime deadline
){
    @AssertTrue(message = "최대 경력은 최소 경력보다 크거나 같아야 합니다.")
    public boolean isMinExperienceValid() {
        if (Objects.isNull(minExperience) || Objects.isNull(maxExperience)) return true;

        return minExperience <= maxExperience;
    }
}
