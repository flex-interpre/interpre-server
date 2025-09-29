package com.flex.interpre.domain.auth.service;

import com.flex.interpre.domain.auth.dto.response.TokenResponse;
import com.flex.interpre.domain.auth.entity.RefreshToken;
import com.flex.interpre.domain.auth.exception.AuthExceptions;
import com.flex.interpre.domain.auth.repository.RefreshTokenRespository;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PreAuthorize("isAuthenticated()")
    public void logout(User user, String token) {

        RefreshToken refreshToken = refreshTokenRespository.findByRefreshToken(token)
                .orElseThrow(AuthExceptions.INVALID_TOKEN::toException);

        if (!refreshToken.getUserId().equals(user.getId())) {
            throw AuthExceptions.INVALID_TOKEN.toException();
        }

        refreshTokenRespository.delete(refreshToken);
    }

    public TokenResponse regenerateToken(String accessToken, String refreshToken) {

        if (Objects.isNull(accessToken)) {
            throw AuthExceptions.INVALID_TOKEN.toException();
        }

        RefreshToken redisRefreshToken = refreshTokenRespository.findByRefreshToken(refreshToken).orElseThrow(AuthExceptions.REFRESH_TOKEN_EXPIRED::toException);

        UUID userId = jwtUtil.extractUUID(accessToken);

        if (!userId.equals(redisRefreshToken.getUserId())) {
            throw AuthExceptions.TOKEN_NOT_PAIR.toException();
        }

        refreshTokenRespository.delete(redisRefreshToken);
        User user = userRepository.findById(userId).orElseThrow(AuthExceptions.INVALID_TOKEN::toException);

        String newAccessToken = jwtUtil.generateToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
