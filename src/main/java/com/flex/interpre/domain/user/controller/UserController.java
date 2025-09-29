package com.flex.interpre.domain.user.controller;


import com.flex.interpre.domain.user.dto.request.UserUpdateRequest;
import com.flex.interpre.domain.user.dto.response.MyUserDetailInfo;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.service.UserService;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회 - Role-> 구직자와 기업 분기")
    public ApiResponse<MyUserDetailInfo> getUserInfo(@AuthenticationPrincipal User user){
        return ApiResponse.ok(userService.getUserInfo(user));
    }

    @PutMapping("/me")
    @Operation(summary = "내 정보 수정")
    public ApiResponse<Void> updateUserInfo(@AuthenticationPrincipal User user, @RequestBody UserUpdateRequest request){
        userService.updateUserInfo(user, request);
        return ApiResponse.ok();
    }
}
