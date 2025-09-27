package com.flex.interpre.domain.user.dto.response;

import com.flex.interpre.domain.user.entity.*;
import lombok.Builder;

import java.util.Set;

@Builder
public record MyJobSeekerInfo(
    String googleId,
    String email,
//    Role role,
    String name,
    Education education,
    Set<Area> desiredAreas,
    Set<JobCategoty> desiredJobCategories
) {
    public static MyJobSeekerInfo from(JobSeeker jobSeeker) {
        if  (jobSeeker == null) {
            return null;
        }
        return MyJobSeekerInfo.builder()
                .googleId(jobSeeker.getUser().getGoogleId())
                .email(jobSeeker.getUser().getEmail())
//                .role(jobSeeker.getUser().getRole())
                .name(jobSeeker.getName())
                .education(jobSeeker.getEducation())
                .desiredAreas(jobSeeker.getDesiredAreas())
                .desiredJobCategories(jobSeeker.getDesiredJobCategories())
                .build();
    }
}
