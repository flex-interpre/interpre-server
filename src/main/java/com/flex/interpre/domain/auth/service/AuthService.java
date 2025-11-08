package com.flex.interpre.domain.auth.service;

import com.flex.interpre.domain.auth.dto.response.TokenResponse;
import com.flex.interpre.domain.auth.entity.RefreshToken;
import com.flex.interpre.domain.auth.exception.AuthExceptions;
import com.flex.interpre.domain.auth.repository.RefreshTokenRespository;
import com.flex.interpre.domain.user.repository.CompanyRepository;
import com.flex.interpre.domain.user.repository.JobSeekerRepository;
import com.flex.interpre.global.constant.Role;
import com.flex.interpre.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRespository refreshTokenRespository;
    private final JobSeekerRepository jobSeekerRepository;
    private final CompanyRepository companyRepository;
    private final JwtUtil jwtUtil;

    @PreAuthorize("isAuthenticated()")
    public void logout(UUID userId, String token) {
        RefreshToken refreshToken = refreshTokenRespository.findByRefreshToken(token)
                .orElseThrow(AuthExceptions.INVALID_TOKEN::toException);

        if (!refreshToken.getUserId().equals(userId)) {
            throw AuthExceptions.INVALID_TOKEN.toException();
        }

        refreshTokenRespository.delete(refreshToken);
    }

    public TokenResponse regenerateToken(String accessToken, String refreshToken) {
        if (Objects.isNull(accessToken)) {
            throw AuthExceptions.INVALID_TOKEN.toException();
        }

        RefreshToken redisRefreshToken = refreshTokenRespository.findByRefreshToken(refreshToken)
                .orElseThrow(AuthExceptions.REFRESH_TOKEN_EXPIRED::toException);

        UUID userId = jwtUtil.extractUUID(accessToken);
        Role role = jwtUtil.extractRole(accessToken);

        if (!userId.equals(redisRefreshToken.getUserId())) {
            throw AuthExceptions.TOKEN_NOT_PAIR.toException();
        }

        boolean exists = verifyAccountExists(userId, role);
        if (!exists) {
            throw AuthExceptions.INVALID_TOKEN.toException();
        }

        // 기존 토큰 삭제
        refreshTokenRespository.delete(redisRefreshToken);

        // 새 토큰 생성
        String newAccessToken = jwtUtil.generateToken(userId, role);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, role);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    private boolean verifyAccountExists(UUID userId, Role role) {
        if (role == Role.JOB_SEEKER) {
            return jobSeekerRepository.existsById(userId);
        } else if (role == Role.COMPANY) {
            return companyRepository.existsById(userId);
        }
        return false;
    }
}
