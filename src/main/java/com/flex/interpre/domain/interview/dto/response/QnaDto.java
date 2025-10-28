package com.flex.interpre.domain.interview.dto.response;

import com.flex.interpre.domain.interview.entity.Qna;
import java.util.UUID;
import lombok.Builder;

@Builder
public record QnaDto(
        UUID id,
        String question,
        String answer
) {
    public static QnaDto from(Qna qna) {
        return QnaDto.builder()
                .id(qna.getId())
                .question(qna.getQuestion())
                .answer(qna.getAnswer())
                .build();
    }
}
