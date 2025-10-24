package com.flex.interpre.domain.interview.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Data
@Builder
@RedisHash(value = "interview_session")
public class InterviewSession {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private UUID userId;

    private UUID interviewId;

    private String contentText;

    @Builder.Default
    private Integer currentQuestionNum = 0;

    @TimeToLive(unit = TimeUnit.HOURS)
    long ttl;
}
