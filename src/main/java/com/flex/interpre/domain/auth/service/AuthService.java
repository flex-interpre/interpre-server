package com.flex.interpre.domain.auth.service;

import com.flex.interpre.domain.auth.entity.RefreshToken;
import com.flex.interpre.domain.auth.exception.AuthExceptions;
import com.flex.interpre.domain.auth.repository.RefreshTokenRespository;
import com.flex.interpre.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRespository refreshTokenRespository;

    @PreAuthorize("isAuthenticated()")
    public void logout(User user, String token) {

        RefreshToken refreshToken = refreshTokenRespository.findByRefreshToken(token)
                .orElseThrow(AuthExceptions.INVALID_TOKEN::toException);

        if (!refreshToken.getUserId().equals(user.getId().toString())) {
            throw AuthExceptions.INVALID_TOKEN.toException();
        }

        refreshTokenRespository.delete(refreshToken);
    }
}
