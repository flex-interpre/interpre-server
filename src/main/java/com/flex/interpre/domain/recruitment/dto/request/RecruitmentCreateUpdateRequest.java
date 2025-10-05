package com.flex.interpre.domain.recruitment.dto.request;

import com.flex.interpre.domain.recruitment.entity.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Set;

public record RecruitmentCreateUpdateRequest(
        @NotBlank String title,
        @NotNull Set<JobGroup> jobGroups,
        @NotNull Set<Job> jobs,
        @NotNull Set<EmploymentType> employmentTypes,
        Integer minExperience,
        Integer maxExperience,
        @NotBlank String location,
        @NotBlank String description,
        Set<String> requirements,
        Set<String> benefits,
        Set<String> skills,
        LocalDateTime deadline
){ }
