package com.flex.interpre.domain.auth.controller;

import com.flex.interpre.domain.auth.service.AuthService;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.global.property.GoogleProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final GoogleProperty googleProperty;
    private final AuthService authService;

    @GetMapping("/google")
    public String googleLogin() {
        String reqUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + googleProperty.getClientId()
                + "&redirect_uri=" + googleProperty.getRedirectUrl() + "&response_type=code&scope=email%20profile%20openid&access_type=offline";

        return "redirect:" + reqUrl;
    }

    @GetMapping("/callback")
    @ResponseBody
    public String callback(@RequestParam("code") String code) {
        return code;
    }

    @PostMapping("/sessions")
    @ResponseBody
    public String session(@RequestParam("code") String code, @RequestParam("role") User.Role role) {
        authService.login(code,role);
        return "";

    }
}
