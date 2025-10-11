package com.flex.interpre.domain.interview.service;

import com.flex.interpre.domain.interview.dto.response.ClovaSttResponse;
import com.flex.interpre.domain.interview.dto.response.SessionResponse;
import com.flex.interpre.domain.interview.entity.Interview;
import com.flex.interpre.domain.interview.entity.InterviewSession;
import com.flex.interpre.domain.interview.exception.InterviewExceptions;
import com.flex.interpre.domain.interview.repository.InterviewRepository;
import com.flex.interpre.domain.interview.repository.InterviewSessionRepository;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.global.property.ClovaProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewRepository interviewRepository;
    private final ClovaProperty clovaProperty;
    private final WebClient webClient;


    @Transactional
    public SessionResponse getSessionResponse(User user) {

        Interview interview = interviewRepository.save(Interview.builder()
                .jobSeeker(user.getJobSeeker())
                .build());

        InterviewSession interviewSession = interviewSessionRepository.save(InterviewSession.builder()
                .userId(user.getId())
                .interviewId(interview.getId())
                .ttl(1)
                .build());
        return new SessionResponse(interviewSession.getId());
    }

    public String transcribe(byte[] audioData) {


        try {
            ClovaSttResponse response = webClient.post()
                    .uri(clovaProperty.getUrl() + "?lang=Kor")
                    .header("X-NCP-APIGW-API-KEY-ID", clovaProperty.getId())
                    .header("X-NCP-APIGW-API-KEY", clovaProperty.getSecret())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .bodyValue(audioData)
                    .retrieve()
                    .bodyToMono(ClovaSttResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();


            if (response == null || response.text() == null || response.text().isEmpty()) {
                throw InterviewExceptions.STT_NO_RESULT.toException();
            }
            return response.text();

        } catch (Exception e) {
            throw InterviewExceptions.STT_PROCESSING_FAILED.toException();
        }
    }
}

