package com.flex.interpre.domain.interview.dto.response;

import lombok.Builder;

@Builder
public record InterviewResponse(
        String transcription,
        String question,
        String audio,
        Integer questionNumber
) {
}
