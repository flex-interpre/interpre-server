package com.flex.interpre.domain.user.jobSeeker.controller;

import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.jobSeeker.dto.resposne.BookMarkListResponse;
import com.flex.interpre.domain.user.jobSeeker.service.JobSeekerService;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/jobseekers")
@PreAuthorize("isAuthenticated()")
public class JobSeekerController {

    private final JobSeekerService jobSeekerService;

    @GetMapping
    @Operation(summary = "북마크 조회 api 요청")
    public ApiResponse<BookMarkListResponse> getBookmarks(@AuthenticationPrincipal User user) {

        return ApiResponse.ok(jobSeekerService.getBookmarks(user));
    }

    @PostMapping("/{recruitment}")
    @Operation(summary = "북마크 추가 api")
    public ApiResponse<Void> addBookmark(@PathVariable Recruitment recruitment, @AuthenticationPrincipal User user) {

        jobSeekerService.addBookmark(recruitment, user);
        return ApiResponse.ok();
    }
}
