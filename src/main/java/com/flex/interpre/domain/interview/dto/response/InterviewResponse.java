package com.flex.interpre.domain.interview.dto.response;

import com.flex.interpre.domain.interview.entity.InterviewReport;
import lombok.Builder;

@Builder
public record InterviewResponse(
        // 이전 사용자 응답 stt 한 결과
        String transcription,
        // 다음 질문
        String question,
        String audio,
        Integer questionNumber,
        InterviewReport interviewReport,
        boolean isFinal
) {
}
