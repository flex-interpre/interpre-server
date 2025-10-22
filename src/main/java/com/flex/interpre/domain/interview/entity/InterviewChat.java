package com.flex.interpre.domain.interview.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@Builder
@RedisHash(value = "interview_chat")
public class InterviewChat {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Indexed
    private UUID interviewId;

    private String question;

    private String answer;

    @TimeToLive(unit = TimeUnit.MINUTES)
    @Builder.Default
    private long ttl = 30;

}
