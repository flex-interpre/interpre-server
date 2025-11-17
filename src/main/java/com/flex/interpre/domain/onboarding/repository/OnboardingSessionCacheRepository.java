package com.flex.interpre.domain.onboarding.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // LocalDateTime 모듈 추가
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 문자열로 변환

    @Value("${onboarding.session-ttl:86400}")
    private long sessionTtl;

    private static final String KEY_PREFIX = "onboarding:session:";

    private String getKey(UUID jobSeekerId) {
        return KEY_PREFIX + jobSeekerId.toString();
    }

    public void save(OnboardingSessionCache session) {
        String key = getKey(UUID.fromString(session.getJobSeekerId()));
        session.setUpdatedAt(LocalDateTime.now());
        redisTemplate.opsForValue().set(key, session, sessionTtl, TimeUnit.SECONDS);
    }

    public Optional<OnboardingSessionCache> findByJobSeekerId(UUID jobSeekerId) {
        String key = getKey(jobSeekerId);
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) return Optional.empty();
        OnboardingSessionCache session =
                objectMapper.convertValue(value, OnboardingSessionCache.class);
        return Optional.of(session);
    }

    public void delete(UUID jobSeekerId) {
        String key = getKey(jobSeekerId);
        redisTemplate.delete(key);
    }

    public boolean exists(UUID jobSeekerId) {
        String key = getKey(jobSeekerId);
        return redisTemplate.hasKey(key);
    }

    public void refreshTtl(UUID jobSeekerId) {
        String key = getKey(jobSeekerId);
        redisTemplate.expire(key, sessionTtl, TimeUnit.SECONDS);
    }
}