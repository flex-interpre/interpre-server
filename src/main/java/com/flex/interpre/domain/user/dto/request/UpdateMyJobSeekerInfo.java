package com.flex.interpre.domain.user.dto.request;


import com.flex.interpre.domain.user.entity.*;

import java.util.Set;

public record UpdateMyJobSeekerInfo(
        String name,
        Education education,
        Set<Area> desiredAreas,
        Set<JobCategory> desiredJobCategories
) implements UserUpdateRequest {
}
