package com.flex.interpre.domain.auth.handler;

import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.repository.UserRepository;
import com.flex.interpre.global.security.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.oauth2.allowed-urls}")
    private List<String> allowedUrls;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String state = request.getParameter("state");
        //role 파라미터 가져오기
        String roleParam = extractRoleFromState(state);
        String callBackUrl = extractCallBackFromState(state);

        User.Role role = User.Role.valueOf(
                roleParam != null ? roleParam.toUpperCase() : "JOB_SEEKER"
        );

        if (!isAllowedUrl(callBackUrl)) {
            //일단은 기본 url
            callBackUrl = "http://localhost:3000";
        }

        Optional<User> optionalUser = userRepository.findByGoogleId((String) oAuth2User.getAttributes().get("sub"));

        boolean firstLogin = optionalUser.isEmpty();
        User user = optionalUser.orElseGet(() -> from(oAuth2User.getAttributes(), role));

        if (role != user.getRole()) {
            response.sendRedirect(callBackUrl);
            return;
        }


        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        setCookie(accessToken, refreshToken, firstLogin, response);

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

    private void setCookie(String accessToken, String refreshToken, boolean firstLogin, HttpServletResponse response) {

        addTokenCookie(response, "accessToken", accessToken);
        addTokenCookie(response, "refreshToken", refreshToken);
        addTokenCookie(response, "firstLogin", String.valueOf(firstLogin));
    }

    private void addTokenCookie(HttpServletResponse response, String name, String value) {

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setSecure(false);
        cookie.setMaxAge(300); // 짧게 5분

        response.addCookie(cookie);
    }

    private String extractRoleFromState(String state) {
        if (state == null) {
            return null;
        }

        try {
            String decodedState = URLDecoder.decode(state, StandardCharsets.UTF_8);

            // role 부분만 파싱
            if (decodedState.contains("|role:")) {
                String[] parts = decodedState.split("\\|role:");
                if (parts.length > 1) {
                    return parts[1].split("\\|")[0];
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private String extractCallBackFromState(String state) {
        if (state == null) return null;
        try {
            String decodedState = URLDecoder.decode(state, StandardCharsets.UTF_8);
            if (decodedState.contains("|callback:")) {
                String[] parts = decodedState.split("\\|callback:");
                if (parts.length > 1) {
                    return parts[1].split("\\|")[0];
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isAllowedUrl(String callBackUrl) {
        if (callBackUrl == null || allowedUrls == null) return false;
        return allowedUrls.stream()
                .anyMatch(callBackUrl::equals);
    }
}
