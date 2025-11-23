package com.flex.interpre.domain.interview.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record InterviewResponse(
        ResponseType type,
        String question,
        String audio,
        String text,
        InterviewReportDto report
) {
    public enum ResponseType {
        QUESTION,
        STT,
        ANSWER_COMPLETE,
        END
    }
}
