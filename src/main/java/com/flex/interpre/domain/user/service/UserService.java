//package com.flex.interpre.domain.user.service;
//
//import com.flex.interpre.domain.user.dto.request.UpdateMyCompanyInfo;
//import com.flex.interpre.domain.user.dto.request.UpdateMyJobSeekerInfo;
//import com.flex.interpre.domain.user.dto.request.UserUpdateRequest;
//import com.flex.interpre.domain.user.dto.response.MyCompanyInfo;
//import com.flex.interpre.domain.user.dto.response.MyJobSeekerInfo;
//import com.flex.interpre.domain.user.dto.response.MyUserDetailInfo;
//import com.flex.interpre.domain.company.entity.Company;
//import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
//import com.flex.interpre.domain.jobSeeker.entity.User;
//import com.flex.interpre.domain.user.exception.UserExceptions;
//import com.flex.interpre.domain.user.repository.CompanyRepository;
//import com.flex.interpre.domain.user.repository.JobSeekerRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//public class UserService {
//    private final JobSeekerRepository jobSeekerRepository;
//    private final CompanyRepository companyRepository;
//
//
//    // 유저 정보 조회
//    @Transactional(readOnly = true)
//    public MyUserDetailInfo getUserInfo(User user) {
//        return switch (user.getRole()) {
//            case JOB_SEEKER -> getJobSeekerInfo(user);
//            case COMPANY -> getCompanyInfo(user);
//            default -> throw UserExceptions.INVALID_ROLE.toException();
//        };
//    }
//
//    // 유저 정보 수정
//    @Transactional
//    public MyUserDetailInfo updateUserInfo(User user, UserUpdateRequest request){
//        return switch (request) {
//            case UpdateMyJobSeekerInfo jobSeekerInfo -> updateJobSeekerInfo(user, jobSeekerInfo);
//            case UpdateMyCompanyInfo companyInfo -> updateCompanyInfo(user, companyInfo);
//            default ->throw UserExceptions.INVALID_ROLE.toException();
//        };
//    }
//
//
//    /*    내부 메서드    */
//
//    private MyJobSeekerInfo getJobSeekerInfo(User user){
//        JobSeeker jobSeeker = jobSeekerRepository.findByUserIdWithUser(user.getId())
//                .orElseThrow(UserExceptions.USER_NOT_FOUND::toException);
//
//        return MyJobSeekerInfo.from(jobSeeker);
//    }
//
//    private MyCompanyInfo getCompanyInfo(User user){
//        Company company = companyRepository.findByUserIdWithUser(user.getId())
//                .orElseThrow(UserExceptions.USER_NOT_FOUND::toException);
//
//        return MyCompanyInfo.from(company);
//    }
//
//    private MyJobSeekerInfo updateJobSeekerInfo(User user, UpdateMyJobSeekerInfo request){
//        JobSeeker jobSeeker = jobSeekerRepository.findByUserIdWithUser(user.getId())
//                .orElseThrow(UserExceptions.USER_NOT_FOUND::toException);
//
//        jobSeeker.update(request);
//
//        return MyJobSeekerInfo.from(jobSeeker);
//    }
//
//    private MyCompanyInfo updateCompanyInfo(User user, UpdateMyCompanyInfo request){
//        Company company = companyRepository.findByUserIdWithUser(user.getId())
//                .orElseThrow(UserExceptions.USER_NOT_FOUND::toException);
//
//        company.update(request);
//
//        return MyCompanyInfo.from(company);
//    }
//}
