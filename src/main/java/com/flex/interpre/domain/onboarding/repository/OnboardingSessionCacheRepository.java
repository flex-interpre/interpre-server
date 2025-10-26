package com.flex.interpre.domain.onboarding.repository;

import com.flex.interpre.domain.onboarding.model.OnboardingSessionCache;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class OnboardingSessionCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${onboarding.session-ttl:86400}")
    private long sessionTtl;

    private static final String KEY_PREFIX = "onboarding:session:";

    private String getKey(UUID userId) {
        return KEY_PREFIX + userId.toString();
    }

    public void save(OnboardingSessionCache session) {
        String key = getKey(UUID.fromString(session.getUserId()));
        session.setUpdatedAt(LocalDateTime.now());
        redisTemplate.opsForValue().set(key, session, sessionTtl, TimeUnit.SECONDS);
    }

    public Optional<OnboardingSessionCache> findByUserId(UUID userId) {
        String key = getKey(userId);
        Object value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable((OnboardingSessionCache) value);
    }

    public void delete(UUID userId) {
        String key = getKey(userId);
        redisTemplate.delete(key);
    }

    public boolean exists(UUID userId) {
        String key = getKey(userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void refreshTtl(UUID userId) {
        String key = getKey(userId);
        redisTemplate.expire(key, sessionTtl, TimeUnit.SECONDS);
    }
}