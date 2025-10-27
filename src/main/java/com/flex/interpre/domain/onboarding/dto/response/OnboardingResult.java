package com.flex.interpre.domain.onboarding.dto.response;

import com.flex.interpre.global.constant.Area;
import com.flex.interpre.global.constant.JobFirst;
import com.flex.interpre.global.constant.JobSecond;
import lombok.Builder;

import java.util.Set;

// 온보딩 최종 결과
@Builder
public record OnboardingResult(
        Set<Area> recommendedAreas,
        Set<JobFirst> recommendedJobFirsts,
        Set<JobSecond> recommendedJobSeconds
//        Set<JobThird> recommendedJobThirds
) {}