package com.flex.interpre.global.handler;

import com.flex.interpre.domain.interview.entity.InterviewSession;
import com.flex.interpre.domain.interview.exception.InterviewExceptions;
import com.flex.interpre.domain.interview.repository.InterviewSessionRepository;
import com.flex.interpre.global.exception.ApiException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InterviewSocketHandler extends TextWebSocketHandler {

    private final InterviewSessionRepository interviewSessionRepository;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {

        String sessionId = getSessionId(session);
        try {
            InterviewSession interviewSession = interviewSessionRepository.findById(UUID.fromString(sessionId))
                    .orElseThrow(InterviewExceptions.INVALID_SESSION_ID::toException);

            //TODO: 자소서 객체가 생성되면 llm으로 자소서 내용 전달.
        } catch (ApiException e) {

            session.close(CloseStatus.NOT_ACCEPTABLE.withReason(e.getMessage()));
        } catch (Exception e) {

            session.close(CloseStatus.SERVER_ERROR.withReason("서버 에러 발생"));
        }


    }

    private String getSessionId(WebSocketSession session) {

        String query = Objects.requireNonNull(session.getUri()).getQuery();
        return query.split("=")[1];
    }
}
