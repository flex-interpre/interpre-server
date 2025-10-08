package com.flex.interpre.domain.interview.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@Builder
@RedisHash(value = "interview_session")
public class InterviewSession {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private UUID userId;

    private UUID interviewId;

    @TimeToLive(unit = TimeUnit.HOURS)
    long ttl;
}
