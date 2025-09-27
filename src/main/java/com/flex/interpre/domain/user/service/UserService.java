package com.flex.interpre.domain.user.service;

import com.flex.interpre.domain.user.dto.response.JobSeekerInfo;
import com.flex.interpre.domain.user.entity.JobSeeker;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.exception.UserExceptions;
import com.flex.interpre.domain.user.repository.JobSeekerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final JobSeekerRepository jobSeekerRepository;

    public JobSeekerInfo getJobSeekerInfo(User user){
        JobSeeker jobSeeker = jobSeekerRepository.findByIdWithUser(user.getId())
                .orElseThrow(UserExceptions.USER_NOT_FOUND::toException);

        return JobSeekerInfo.from(jobSeeker);
    }
}
