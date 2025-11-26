package com.flex.interpre.domain.jobSeeker.controller;

import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.jobSeeker.service.BookmarkService;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentSummaryResponse;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.jobSeeker.service.JobSeekerService;
import com.flex.interpre.domain.jobSeeker.dto.UpdateMyJobSeekerInfo;
import com.flex.interpre.domain.jobSeeker.dto.MyJobSeekerInfo;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/jobseekers")
@PreAuthorize("hasRole('JOB_SEEKER')")
public class JobSeekerController {
    private final JobSeekerService jobSeekerService;
    private final BookmarkService bookmarkService;

    @GetMapping("/me")
    @Operation(summary = "내 구직자 정보 조회")
    public ApiResponse<MyJobSeekerInfo> getMyInfo(
            @AuthenticationPrincipal @Parameter(hidden = true) JobSeeker jobSeeker) {
        return ApiResponse.ok(MyJobSeekerInfo.from(jobSeeker));
    }

    @PutMapping("/me")
    @Operation(summary = "내 구직자 정보 수정")
    public ApiResponse<MyJobSeekerInfo> updateMyInfo(@AuthenticationPrincipal @Parameter(hidden = true) JobSeeker jobSeeker,
                                                     @Valid @RequestBody UpdateMyJobSeekerInfo request) {
        return ApiResponse.ok(jobSeekerService.updateMyInfo(jobSeeker, request));
    }

    @GetMapping("/bookmarks")
    @Operation(summary = "북마크 조회 api 요청")
    public ApiResponse<List<RecruitmentSummaryResponse>> getBookmarks(@AuthenticationPrincipal @Parameter(hidden = true) JobSeeker jobSeeker) {
        return ApiResponse.ok(bookmarkService.getBookmarks(jobSeeker));
    }

    @PostMapping("/bookmarks/{recruitment}")
    @Operation(summary = "북마크 추가 api")
    public ApiResponse<Void> addBookmark(@AuthenticationPrincipal @Parameter(hidden = true) JobSeeker jobSeeker,
                                         @PathVariable UUID recruitmentId) {
        bookmarkService.addBookmark(jobSeeker, recruitmentId);
        return ApiResponse.ok();
    }

    @DeleteMapping("/bookmarks/{recruitment}")
    @Operation(summary = "북마크 제거 api")
    public ApiResponse<Void> deleteBookmark(@AuthenticationPrincipal @Parameter(hidden = true) JobSeeker jobSeeker,
                                            @PathVariable UUID recruitmentId) {
        bookmarkService.removeBookmark(jobSeeker, recruitmentId);
        return ApiResponse.ok();
    }
}
