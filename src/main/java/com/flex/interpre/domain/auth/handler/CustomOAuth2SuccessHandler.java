package com.flex.interpre.domain.auth.handler;

import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.repository.UserRepository;
import com.flex.interpre.global.security.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String roleParam = request.getParameter("role");
        User.Role role = User.Role.valueOf(
                roleParam != null ? roleParam.toUpperCase() : "JOB_SEEKER"
        );


        User user = userRepository.findByGoogleId((String) oAuth2User.getAttributes().get("sub")).orElseGet(() -> from(oAuth2User.getAttributes(),role));

        String accessToken =  jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        setCookie(accessToken,refreshToken,response);

        response.sendRedirect("http://localhost:3000/callback");

        
    }

    private User from(Map<String, Object> attributes, User.Role role) {

        boolean approved = true;
        if (role == User.Role.COMPANY) approved = false;

        return userRepository.save(
                User.builder()
                        .email((String) attributes.get("email"))
                        .googleId((String) attributes.get("sub"))
                        .role(role)
                        .approved(approved)
                        .build()
        );
    }

    private void setCookie(String accessToken, String refreshToken, HttpServletResponse response) {

        addTokenCookie(response, "accessToken", accessToken);
        addTokenCookie(response, "refreshToken", refreshToken);
    }

    private void addTokenCookie(HttpServletResponse response, String name, String value) {

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setSecure(false);
        cookie.setMaxAge(300); // 짧게 5분

        response.addCookie(cookie);
    }
}
