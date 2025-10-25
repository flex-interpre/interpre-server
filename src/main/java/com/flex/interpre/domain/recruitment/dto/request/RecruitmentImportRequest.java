package com.flex.interpre.domain.recruitment.dto.request;

import com.flex.interpre.global.constant.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

// 외부 공고문 데이터 임포트용 DTO
@Builder
public record RecruitmentImportRequest(
        String companyName,
        String title,
        String description,
        LocalDateTime deadline,
        Set<String> jobAreas,
        String location,
        Set<String> jobFirsts,
        Set<String> jobSeconds,
        Set<String> jobThirds,
        Set<String> employmentTypes,
        Integer minExperience,
        Integer maxExperience,
        Set<String> requirements,
        Set<String> benefits,
        Set<String> skills
) {}

