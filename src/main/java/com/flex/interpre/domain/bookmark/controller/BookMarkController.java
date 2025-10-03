package com.flex.interpre.domain.bookmark.controller;

import com.flex.interpre.domain.bookmark.dto.response.BookMarkListResponse;
import com.flex.interpre.domain.bookmark.service.BookMarkService;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookmarks")
@PreAuthorize("isAuthenticated()")
public class BookMarkController {

    private final BookMarkService bookmarkService;

    @GetMapping
    @Operation(summary = "북마크 조회 api 요청")
    public ApiResponse<BookMarkListResponse> getBookmarks(@AuthenticationPrincipal User user) {

        return ApiResponse.ok(bookmarkService.getBookmarks(user));
    }
}
