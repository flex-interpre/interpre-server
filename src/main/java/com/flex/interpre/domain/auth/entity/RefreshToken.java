package com.flex.interpre.domain.auth.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import lombok.Builder;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.concurrent.TimeUnit;

@Builder
@Getter
@RedisHash(value = "refresh_token")
public class RefreshToken {

    @Id
    Long id;

    String userId;

    @Indexed
    String refreshToken;

    @TimeToLive(unit = TimeUnit.HOURS)
    long ttl;
}
