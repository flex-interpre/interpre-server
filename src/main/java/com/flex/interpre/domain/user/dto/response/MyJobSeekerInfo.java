package com.flex.interpre.domain.user.dto.response;

import com.flex.interpre.domain.user.entity.*;
import jakarta.annotation.Nonnull;
import lombok.Builder;

import java.util.Set;

@Builder
public record MyJobSeekerInfo(
    String googleId,
    String email,
    String name,
    Education education,
    Set<Area> desiredAreas,
    Set<JobCategoty> desiredJobCategories
) implements MyUserDetailInfo {
    public static MyJobSeekerInfo from(@Nonnull JobSeeker jobSeeker) {
        return MyJobSeekerInfo.builder()
                .googleId(jobSeeker.getUser().getGoogleId())
                .email(jobSeeker.getUser().getEmail())
                .name(jobSeeker.getName())
                .education(jobSeeker.getEducation())
                .desiredAreas(jobSeeker.getDesiredAreas())
                .desiredJobCategories(jobSeeker.getDesiredJobCategories())
                .build();
    }
}
