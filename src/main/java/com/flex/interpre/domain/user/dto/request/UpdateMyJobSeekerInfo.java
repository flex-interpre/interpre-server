package com.flex.interpre.domain.user.dto.request;


import com.flex.interpre.domain.user.entity.Area;
import com.flex.interpre.domain.user.entity.Education;
import com.flex.interpre.domain.user.entity.JobCategoty;

import java.util.Set;

public record UpdateMyJobSeekerInfo(
        String name,
        Education education,
        Set<Area> desiredAreas,
        Set<JobCategoty> desiredJobCategories
) implements UserUpdateRequest {
}
