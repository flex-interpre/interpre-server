package com.flex.interpre.domain.user.service;

import com.flex.interpre.domain.user.dto.request.UpdateMyCompanyInfo;
import com.flex.interpre.domain.user.dto.request.UpdateMyJobSeekerInfo;
import com.flex.interpre.domain.user.dto.request.UserUpdateRequest;
import com.flex.interpre.domain.user.dto.response.MyCompanyInfo;
import com.flex.interpre.domain.user.dto.response.MyJobSeekerInfo;
import com.flex.interpre.domain.user.dto.response.MyUserDetailInfo;
import com.flex.interpre.domain.user.entity.Company;
import com.flex.interpre.domain.user.entity.JobSeeker;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.exception.UserExceptions;
import com.flex.interpre.domain.user.repository.CompanyRepository;
import com.flex.interpre.domain.user.repository.JobSeekerRepository;
import com.flex.interpre.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final JobSeekerRepository jobSeekerRepository;
    private final CompanyRepository companyRepository;


    // 유저 정보 조회
    @Transactional(readOnly = true)
    public MyUserDetailInfo getUserInfo(User user) {
        return switch (user.getRole()) {
            case JOB_SEEKER -> getJobSeekerInfo(user);
            case COMPANY -> getCompanyInfo(user);
            default -> throw new ApiException(UserExceptions.INVALID_ROLE);
        };
    }

    // 유저 정보 수정
    @Transactional
    public void updateUserInfo(User user, UserUpdateRequest request){
        switch (request) {
            case UpdateMyJobSeekerInfo jobSeekerInfo -> updateJobSeekerInfo(user, jobSeekerInfo);
            case UpdateMyCompanyInfo companyInfo -> updateCompanyInfo(user, companyInfo);
            default -> throw new ApiException(UserExceptions.INVALID_ROLE);
        }
    }


    /*    내부 메서드    */

    private MyJobSeekerInfo getJobSeekerInfo(User user){
        JobSeeker jobSeeker = jobSeekerRepository.findByIdWithUser(user.getId())
                .orElseThrow(UserExceptions.USER_NOT_FOUND::toException);

        return MyJobSeekerInfo.from(jobSeeker);
    }

    private MyCompanyInfo getCompanyInfo(User user){
        Company company = companyRepository.findByIdWithUser(user.getId())
                .orElseThrow(UserExceptions.USER_NOT_FOUND::toException);

        return MyCompanyInfo.from(company);
    }

    private void updateJobSeekerInfo(User user, UpdateMyJobSeekerInfo request){
        JobSeeker jobSeeker = jobSeekerRepository.findByIdWithUser(user.getId())
                .orElseThrow(UserExceptions.USER_NOT_FOUND::toException);

        jobSeeker.setName(request.name());
        jobSeeker.setEducation(request.education());
        jobSeeker.setDesiredAreas(request.desiredAreas());
        jobSeeker.setDesiredJobCategories(request.desiredJobCategories());
    }

    private void updateCompanyInfo(User user, UpdateMyCompanyInfo request){
        Company company = companyRepository.findByIdWithUser(user.getId())
                .orElseThrow(UserExceptions.USER_NOT_FOUND::toException);

        company.setCompanyName(request.companyName());
        company.setBusinessNumber(request.businessNumber());
        company.setAddress(request.address());
        company.setWebsite(request.website());
        company.setDescription(request.description());
        company.setLogoUrl(request.logoUrl());

    }
}
