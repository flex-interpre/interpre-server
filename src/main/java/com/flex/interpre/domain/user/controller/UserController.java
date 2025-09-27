package com.flex.interpre.domain.user.controller;

import com.flex.interpre.domain.user.dto.response.MyCompanyInfo;
import com.flex.interpre.domain.user.dto.response.MyJobSeekerInfo;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.service.UserService;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 (구직자) 정보 조회")
    public ApiResponse<MyJobSeekerInfo> getJobSeekerInfo(@AuthenticationPrincipal User user){
        return ApiResponse.ok(userService.getJobSeekerInfo(user));
    }

    @GetMapping("/company/me")
    @Operation(summary = "내 기업 정보 조회")
    public ApiResponse<MyCompanyInfo> getCompanyInfo(@AuthenticationPrincipal User user){
        return ApiResponse.ok(userService.getCompanyInfo(user));
    }
}
