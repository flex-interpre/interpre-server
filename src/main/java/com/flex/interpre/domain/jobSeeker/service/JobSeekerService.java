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
    private final JobSeekerProfileVectorService jobSeekerProfileVectorService;

    @Transactional
    public MyJobSeekerInfo updateMyInfo(JobSeeker jobSeeker, UpdateMyJobSeekerInfo request) {
        jobSeeker.update(request);

        // 온보딩 기반 벡터 반영
        jobSeekerProfileVectorService.updateProfileEmbedding(jobSeeker.getId());

        return MyJobSeekerInfo.from(jobSeeker);
    }
}
