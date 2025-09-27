package com.flex.interpre.domain.user.service;

import com.flex.interpre.domain.user.dto.response.MyCompanyInfo;
import com.flex.interpre.domain.user.dto.response.MyJobSeekerInfo;
import com.flex.interpre.domain.user.entity.Company;
import com.flex.interpre.domain.user.entity.JobSeeker;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.exception.UserExceptions;
import com.flex.interpre.domain.user.repository.CompanyRepository;
import com.flex.interpre.domain.user.repository.JobSeekerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final JobSeekerRepository jobSeekerRepository;
    private final CompanyRepository companyRepository;

    public MyJobSeekerInfo getJobSeekerInfo(User user){
        JobSeeker jobSeeker = jobSeekerRepository.findByIdWithUser(user.getId())
                .orElseThrow(UserExceptions.USER_NOT_FOUND::toException);

        return MyJobSeekerInfo.from(jobSeeker);
    }

    public MyCompanyInfo getCompanyInfo(User user){
        Company company = companyRepository.findByIdWithUser(user.getId())
                .orElseThrow(UserExceptions.USER_NOT_FOUND::toException);

        return MyCompanyInfo.from(company);
    }
}
