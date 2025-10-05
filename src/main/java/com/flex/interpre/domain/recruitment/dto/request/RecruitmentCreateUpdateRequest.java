package com.flex.interpre.domain.recruitment.dto.request;

import com.flex.interpre.domain.recruitment.entity.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
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
        LocalDateTime deadline
){ }
