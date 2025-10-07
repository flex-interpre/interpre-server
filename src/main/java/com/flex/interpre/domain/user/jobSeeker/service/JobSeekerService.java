package com.flex.interpre.domain.user.jobSeeker.service;

import com.flex.interpre.domain.recruitment.dto.response.RecruitmentSummaryResponse;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.user.entity.JobSeeker;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.repository.JobSeekerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('JOB_SEEKER')")
public class JobSeekerService {

    private final JobSeekerRepository jobSeekerRepository;

    @Transactional(readOnly = true)
    public List<RecruitmentSummaryResponse> getBookmarks(User user) {

        JobSeeker jobSeeker = user.getJobSeeker();
        return jobSeeker.getBookmarkedRecruitments().stream().map(RecruitmentSummaryResponse::from).toList();

    }

    @Transactional
    public void addBookmark(Recruitment recruitment, @AuthenticationPrincipal User user) {

        JobSeeker jobSeeker = user.getJobSeeker();

        jobSeeker.getBookmarkedRecruitments().add(recruitment);
        jobSeekerRepository.save(jobSeeker);
    }

    @Transactional
    public void deleteBookmark(Recruitment recruitment, @AuthenticationPrincipal User user) {

        JobSeeker jobSeeker = user.getJobSeeker();

        jobSeeker.getBookmarkedRecruitments().remove(recruitment);
        jobSeekerRepository.save(jobSeeker);
    }

}
