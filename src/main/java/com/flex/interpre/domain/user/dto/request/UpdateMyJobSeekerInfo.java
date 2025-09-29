package com.flex.interpre.domain.user.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flex.interpre.domain.user.entity.Area;
import com.flex.interpre.domain.user.entity.Education;
import com.flex.interpre.domain.user.entity.JobCategoty;
import com.flex.interpre.domain.user.entity.Role;

import java.util.Set;

public record UpdateMyJobSeekerInfo(
        String name,
        Education education,
        Set<Area> desiredAreas,
        Set<JobCategoty> desiredJobCategories
) implements UserUpdateRequest {

    @JsonIgnore  // JSON 응답에는 포함 안함
    public Role getRole() {
        return Role.JOB_SEEKER;
    }
}
