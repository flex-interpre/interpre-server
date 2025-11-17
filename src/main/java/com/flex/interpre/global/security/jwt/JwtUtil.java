package com.flex.interpre.global.security.jwt;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.flex.interpre.domain.auth.entity.RefreshToken;
import com.flex.interpre.domain.auth.exception.AuthExceptions;
import com.flex.interpre.domain.auth.repository.RefreshTokenRespository;
import com.flex.interpre.global.constant.Role;
import com.flex.interpre.global.property.JwtProperty;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperty jwtProperty;
    private final RefreshTokenRespository refreshTokenRespository;

    @Bean
    public Algorithm algorithm() {

        return Algorithm.HMAC256(jwtProperty.getKey());
    }

    public String extractToken(HttpServletRequest request) {

        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {

            return authorization.substring(7);

        } else return null;
    }

    public boolean validateToken(String token) {

        if (Objects.isNull(token)) {
            return false;
        }

        try {

            JWT.require(algorithm()).build().verify(token);
            return true;
        } catch (TokenExpiredException e) {

            throw AuthExceptions.ACCESS_TOKEN_EXPIRED.toException();
        } catch (Exception e) {

            return false;
        }

    }

    public UUID extractUUID(String token) {

        return UUID.fromString(
                JWT.require(algorithm())
                        .build()
                        .verify(token)
                        .getClaim("id")
                        .asString()
        );
    }

    public Role extractRole(String token) {
        String roleStr = JWT.require(algorithm())
                .build()
                .verify(token)
                .getClaim("role")
                .asString();

        return Role.valueOf(roleStr);
    }

    public String generateToken(UUID id, Role role) {
        return JWT.create()
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(jwtProperty.getTokenExpiration(), ChronoUnit.HOURS))
                .withClaim("id", id.toString())
                .withClaim("role", role.name())
                .withClaim("type", TokenType.ACCESS.name())
                .sign(algorithm());
    }
//    public String generateToken(User user) {
//        return JWT.create()
//                .withIssuedAt(Instant.now())
//                .withExpiresAt(Instant.now().plus(jwtProperty.getTokenExpiration(), ChronoUnit.HOURS))
//                .withClaim("id", user.getId().toString())
//                .withClaim("role", user.getRole().name())
//                .withClaim("type", TokenType.ACCESS.name())
//                .sign(algorithm());
//    }

    public String generateRefreshToken(UUID id, Role role) {
        String token = JWT.create()
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(jwtProperty.getRefreshExpiration(), ChronoUnit.HOURS))
                .withClaim("id", id.toString())
                .withClaim("role", role.name())
                .withClaim("type", TokenType.REFRESH.name())
                .sign(algorithm());

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(id)
                .refreshToken(token)
                .ttl(jwtProperty.getRefreshExpiration())
                .build();

        refreshTokenRespository.save(refreshToken);

        return token;
    }

    public enum TokenType {
        ACCESS,
        REFRESH
    }
}
