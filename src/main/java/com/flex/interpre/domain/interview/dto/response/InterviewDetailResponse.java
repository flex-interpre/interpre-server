package com.flex.interpre.domain.interview.dto.response;

import com.flex.interpre.domain.interview.entity.Qna;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record InterviewDetailResponse(
        UUID id,
        LocalDateTime createdAt,
        String title,
        Long durationSeconds,
        List<Qna> qna
) {
}
