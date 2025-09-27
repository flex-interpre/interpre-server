package com.flex.interpre.domain.user.controller;

import com.flex.interpre.domain.user.dto.response.MyCompanyInfo;
import com.flex.interpre.domain.user.dto.response.MyJobSeekerInfo;
import com.flex.interpre.domain.user.dto.response.MyUserDetailInfo;
import com.flex.interpre.domain.user.entity.Role;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.exception.UserExceptions;
import com.flex.interpre.domain.user.service.UserService;
import com.flex.interpre.global.dto.ApiResponse;
import com.flex.interpre.global.exception.ApiException;
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
    @Operation(summary = "내 정보 조회 - Role-> 구직자와 기업 분기")
    public ApiResponse<MyUserDetailInfo> getJobSeekerInfo(@AuthenticationPrincipal User user){
        return switch (user.getRole()) {
            case JOB_SEEKER -> ApiResponse.ok(userService.getJobSeekerInfo(user));
            case COMPANY   -> ApiResponse.ok(userService.getCompanyInfo(user));
            default        -> throw new ApiException(UserExceptions.INVALID_ROLE);
        };
    }
}
