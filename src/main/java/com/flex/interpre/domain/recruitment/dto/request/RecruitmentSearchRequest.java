package com.flex.interpre.domain.recruitment.dto.request;

import com.flex.interpre.global.constant.Area;
import com.flex.interpre.global.constant.EmploymentType;
import com.flex.interpre.global.constant.JobFirst;
import com.flex.interpre.global.constant.JobSecond;
import com.flex.interpre.global.constant.JobThird;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "공고 검색 및 필터링 요청")
public record RecruitmentSearchRequest(
        @Schema(description = "검색 키워드") String keyword,
        @Schema(description = "제외할 키워드") String excludeKeyword,
        @Schema(description = "검색할 필드 (title, description, companyName 등)") Set<String> searchFields,
        @Schema(description = "직종 1차") Set<JobFirst> jobFirsts,
        @Schema(description = "직종 2차") Set<JobSecond> jobSeconds,
        @Schema(description = "직종 3차") Set<JobThird> jobThirds,
        @Schema(description = "지역") Set<Area> jobAreas,
        @Schema(description = "고용형태") Set<EmploymentType> employmentTypes,
        @Schema(description = "최소 경력") Integer minExperience,
        @Schema(description = "최대 경력") Integer maxExperience,
        @Schema(description = "정렬 기준 (latest, popular, deadline 등)") String sort,
        @Schema(description = "페이지") Integer page,
        @Schema(description = "페이지 크기") Integer size
) {}
