package com.flex.interpre.domain.interview.service;

import com.flex.interpre.domain.interview.dto.response.SessionResponse;
import com.flex.interpre.domain.interview.entity.Interview;
import com.flex.interpre.domain.interview.entity.InterviewSession;
import com.flex.interpre.domain.interview.repository.InterviewRepository;
import com.flex.interpre.domain.interview.repository.InterviewSessionRepository;
import com.flex.interpre.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewRepository interviewRepository;

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
}
