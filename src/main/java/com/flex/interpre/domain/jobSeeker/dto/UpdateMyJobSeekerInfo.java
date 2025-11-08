package com.flex.interpre.domain.jobSeeker.dto;


import com.flex.interpre.domain.jobSeeker.entity.Education;
import com.flex.interpre.global.constant.Area;
import com.flex.interpre.global.constant.JobFirst;
import com.flex.interpre.global.constant.JobSecond;
import com.flex.interpre.global.constant.JobThird;

import java.util.Set;

public record UpdateMyJobSeekerInfo(
        String name,
        Education education,
        Set<Area> desiredAreas,
        Set<JobFirst> jobFirsts,
        Set<JobSecond> jobSeconds,
        Set<JobThird> jobThirds
){
}
