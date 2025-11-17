package com.flex.interpre.domain.jobSeeker.service;

import com.flex.interpre.domain.recruitment.dto.response.RecruitmentSummaryResponse;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.jobSeeker.dto.UpdateMyJobSeekerInfo;
import com.flex.interpre.domain.jobSeeker.dto.MyJobSeekerInfo;
import com.flex.interpre.domain.jobSeeker.repository.JobSeekerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('JOB_SEEKER')")
public class JobSeekerService {
    private final JobSeekerRepository jobSeekerRepository;

    @Transactional
    public MyJobSeekerInfo updateMyInfo(JobSeeker jobSeeker, UpdateMyJobSeekerInfo request) {
        jobSeeker.update(request);

        return MyJobSeekerInfo.from(jobSeeker);
    }

    @Transactional(readOnly = true)
    public List<RecruitmentSummaryResponse> getBookmarks(JobSeeker jobSeeker) {
        JobSeeker jobSeekerWithBookmarks = jobSeekerRepository.findByIdWithBookmarks(jobSeeker.getId())
                .orElseThrow();

        return jobSeekerWithBookmarks.getBookmarkedRecruitments().stream().map(RecruitmentSummaryResponse::from).toList();
    }

    @Transactional
    public void addBookmark(JobSeeker jobSeeker, Recruitment recruitment) {
        JobSeeker jobSeekerWithBookmarks = jobSeekerRepository.findByIdWithBookmarks(jobSeeker.getId())
                .orElseThrow();

        jobSeekerWithBookmarks.getBookmarkedRecruitments().add(recruitment);
        jobSeekerRepository.save(jobSeekerWithBookmarks);
    }

    @Transactional
    public void deleteBookmark(JobSeeker jobSeeker, Recruitment recruitment) {
        JobSeeker jobSeekerWithBookmarks = jobSeekerRepository.findByIdWithBookmarks(jobSeeker.getId())
                .orElseThrow();

        jobSeekerWithBookmarks.getBookmarkedRecruitments().remove(recruitment);
        jobSeekerRepository.save(jobSeekerWithBookmarks);
    }
}
