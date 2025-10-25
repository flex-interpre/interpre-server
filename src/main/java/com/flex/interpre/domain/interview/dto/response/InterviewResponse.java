package com.flex.interpre.domain.interview.dto.response;

import lombok.Builder;

@Builder
public record InterviewResponse(
        // 이전 사용자 응답 stt 한 결과
        String transcription,
        // 다음 질문
        String question,
        String audio,
        Integer questionNumber,
        boolean isFinal
) {
}
