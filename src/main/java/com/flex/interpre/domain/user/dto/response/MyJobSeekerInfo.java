package com.flex.interpre.domain.user.dto.response;

import com.flex.interpre.domain.jobSeeker.entity.Education;
import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.global.constant.Area;
import com.flex.interpre.global.constant.JobFirst;
import com.flex.interpre.global.constant.JobSecond;
import com.flex.interpre.global.constant.JobThird;
import jakarta.annotation.Nonnull;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
public record MyJobSeekerInfo(
    UUID id,
    String email,
    String name,
    Education education,
    Set<Area> desiredAreas,
    Set<JobFirst> jobFirsts,
    Set<JobSecond> jobSeconds,
    Set<JobThird> jobThirds
){
    public static MyJobSeekerInfo from(@Nonnull JobSeeker jobSeeker) {
        return MyJobSeekerInfo.builder()
                .id(jobSeeker.getId())
                .email(jobSeeker.getEmail())
                .name(jobSeeker.getName())
                .education(jobSeeker.getEducation())
                .desiredAreas(jobSeeker.getDesiredAreas())
                .jobFirsts(jobSeeker.getJobFirsts())
                .jobSeconds(jobSeeker.getJobSeconds())
                .jobThirds(jobSeeker.getJobThirds())
                .build();
    }
}
