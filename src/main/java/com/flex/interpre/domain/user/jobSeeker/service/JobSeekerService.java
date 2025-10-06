package com.flex.interpre.domain.user.jobSeeker.service;

import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.user.entity.JobSeeker;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.exception.UserExceptions;
import com.flex.interpre.domain.user.jobSeeker.dto.resposne.BookMarkListResponse;
import com.flex.interpre.domain.user.repository.JobSeekerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('JOB_SEEKER')")
public class JobSeekerService {

    private final JobSeekerRepository jobSeekerRepository;

    @Transactional(readOnly = true)
    public BookMarkListResponse getBookmarks(User user) {

        JobSeeker jobSeeker = jobSeekerRepository.findByIdWithUser(user.getId()).orElseThrow(UserExceptions.USER_NOT_FOUND::toException);
        return BookMarkListResponse.from(jobSeeker.getBookmarkedRecruitments());

    }

    @Transactional
    public void addBookmark(Recruitment recruitment, @AuthenticationPrincipal User user) {

        JobSeeker jobSeeker = jobSeekerRepository.findByIdWithUser(user.getId()).orElseThrow(UserExceptions.USER_NOT_FOUND::toException);

        jobSeeker.getBookmarkedRecruitments().add(recruitment);
        jobSeekerRepository.save(jobSeeker);
    }

    @Transactional
    public void deleteBookmark(Recruitment recruitment, @AuthenticationPrincipal User user) {

        JobSeeker jobSeeker = jobSeekerRepository.findByIdWithUser(user.getId()).orElseThrow(UserExceptions.USER_NOT_FOUND::toException);

        jobSeeker.getBookmarkedRecruitments().remove(recruitment);
        jobSeekerRepository.save(jobSeeker);
    }

}
