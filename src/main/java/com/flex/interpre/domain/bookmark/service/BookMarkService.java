package com.flex.interpre.domain.bookmark.service;

import com.flex.interpre.domain.bookmark.dto.response.BookMarkListResponse;
import com.flex.interpre.domain.user.entity.JobSeeker;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.exception.UserExceptions;
import com.flex.interpre.domain.user.repository.JobSeekerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('JOB_SEEKER')")
public class BookMarkService {

    private final JobSeekerRepository jobSeekerRepository;

    public BookMarkListResponse getBookmarks(User user) {

        JobSeeker jobSeeker = jobSeekerRepository.findByIdWithUser(user.getId()).orElseThrow(UserExceptions.USER_NOT_FOUND::toException);

        return BookMarkListResponse.from(jobSeeker.getBookMarks());
    }
}
