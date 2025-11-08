package com.flex.interpre.global.handler;

import com.flex.interpre.domain.company.entity.Company;
import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.global.constant.Role;
import com.flex.interpre.domain.company.repository.CompanyRepository;
import com.flex.interpre.domain.jobSeeker.repository.JobSeekerRepository;
import com.flex.interpre.global.property.UrlProperty;
import com.flex.interpre.global.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UrlProperty urlProperty;
    private final CompanyRepository companyRepository;
    private final JobSeekerRepository jobSeekerRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String state = request.getParameter("state");

        //role 파라미터 가져오기
        String roleParam = extractRoleFromState(state);
        String callBackUrl = extractCallBackFromState(state);
        Role role = Role.valueOf(
                roleParam != null ? roleParam.toUpperCase() : "JOB_SEEKER"
        );

        if (!isAllowedUrl(callBackUrl)) {
            callBackUrl = "http://localhost:3000"; //일단은 기본 url
        }

//        Optional<User> optionalUser = userRepository.findByGoogleId((String) oAuth2User.getAttributes().get("sub"));
        String googleId = (String) oAuth2User.getAttributes().get("sub");
        String email = (String) oAuth2User.getAttributes().get("email");
        String name = (String) oAuth2User.getAttributes().get("name");

//        boolean firstLogin = optionalUser.isEmpty();
        UUID accountId;
        boolean firstLogin;

//        User user = optionalUser.orElseGet(() -> from(oAuth2User.getAttributes(), role));
        if (role == Role.JOB_SEEKER) {
            Optional<JobSeeker> optionalJobSeeker = jobSeekerRepository.findByGoogleId(googleId);
            firstLogin = optionalJobSeeker.isEmpty();

            // 최초 로그인이면 생성
            JobSeeker jobSeeker = optionalJobSeeker.orElseGet(() ->
                    createJobSeeker(email, googleId, name));

            accountId = jobSeeker.getId();

        } else { // COMPANY
            Optional<Company> optionalCompany = companyRepository.findByGoogleId(googleId);
            firstLogin = optionalCompany.isEmpty();

            // 최초 로그인이면 생성
            Company company = optionalCompany.orElseGet(() ->
                    createCompany(email, googleId));

            accountId = company.getId();
        }

//        if (role != user.getRole()) {
//            response.sendRedirect(callBackUrl);
//            return;
//        }

//        String accessToken = jwtUtil.generateToken(user);
//        String refreshToken = jwtUtil.generateRefreshToken(user);
        // 해당 Id와 role을 포함한 토큰 생성
        String accessToken = jwtUtil.generateToken(accountId, role);
        String refreshToken = jwtUtil.generateRefreshToken(accountId, role);

        String redirectUrl = buildRedirectUrl(callBackUrl, accessToken, refreshToken, firstLogin);

        response.sendRedirect(redirectUrl);
    }

    // ** 내부 메서드 **

    // 구직자 최초 로그인 생성
    private JobSeeker createJobSeeker(String email, String googleId, String name) {
        JobSeeker jobSeeker = JobSeeker.builder()
                .email(email)
                .googleId(googleId)
                .name(name)
                .role(Role.JOB_SEEKER)
                .approved(true)
                .build();

        return jobSeekerRepository.save(jobSeeker);
    }

    // 기업 최초 로그인 생성
    private Company createCompany(String email, String googleId) {
        Company company = Company.builder()
                .email(email)
                .googleId(googleId)
                .role(Role.COMPANY)
                .approved(false)
                .build();

        return companyRepository.save(company);
    }

//    private User from(Map<String, Object> attributes, Role role) {
//
//        boolean approved = role != Role.COMPANY;
//
//        User user = userRepository.save(User.builder()
//                .email((String) attributes.get("email"))
//                .googleId((String) attributes.get("sub"))
//                .role(role)
//                .approved(approved)
//                .build());
//
//        if (role == Role.COMPANY) {
//            Company company = Company.builder()
//                    .user(user)
//                    .build();
//            companyRepository.save(company);
//
//        } else if (role == Role.JOB_SEEKER) {
//            JobSeeker jobSeeker = JobSeeker.builder()
//                    .user(user)
//                    .name((String) attributes.get("name"))
//                    .build();
//            jobSeekerRepository.save(jobSeeker);
//        }
//
//        return user;
//    }

    private String buildRedirectUrl(String baseUrl, String accessToken, String refreshToken, boolean firstLogin) {
        StringBuilder url = new StringBuilder(baseUrl);

        // 이미 쿼리 파라미터가 있는지 확인
        String separator = baseUrl.contains("?") ? "&" : "?";

        url.append(separator)
                .append("accessToken=").append(URLEncoder.encode(accessToken, StandardCharsets.UTF_8))
                .append("&refreshToken=").append(URLEncoder.encode(refreshToken, StandardCharsets.UTF_8))
                .append("&firstLogin=").append(firstLogin);

        return url.toString();
    }

    private String extractRoleFromState(String state) {
        if (state == null) { return null; }

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
        if (state == null) { return null; }

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
        if (callBackUrl == null || urlProperty.getAllowedUrls() == null) {
            return false;
        }
        return urlProperty.getAllowedUrls().contains(callBackUrl);
    }
}
