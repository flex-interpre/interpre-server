package com.flex.interpre.domain.interview.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED, staticName = "of")
public class Qna {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id")
    private Interview interview;

    public static Qna from(InterviewChat interviewChat, Interview interview) {

        return Qna.builder()
                .interview(interview)
                .question(interviewChat.getQuestion())
                .answer(interviewChat.getAnswer() != null ? interviewChat.getAnswer() : "") // null 체크
                .build();
    }
}
