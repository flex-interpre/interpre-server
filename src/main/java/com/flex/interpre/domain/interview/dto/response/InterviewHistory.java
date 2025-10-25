package com.flex.interpre.domain.interview.dto.response;

import com.flex.interpre.domain.interview.entity.Interview;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record InterviewHistory(
        UUID id,
        LocalDateTime createdAt,
        String title,
        Long durationSecond

) {
    public static InterviewHistory from(Interview interview) {
        return InterviewHistory.builder()
                .id(interview.getId())
                .createdAt(interview.getCreatedAt())
                .title(interview.getTitle())
                .durationSecond(interview.getDurationSecond())
                .build();
    }
}
