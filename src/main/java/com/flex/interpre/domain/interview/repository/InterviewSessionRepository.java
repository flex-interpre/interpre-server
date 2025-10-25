package com.flex.interpre.domain.interview.repository;

import com.flex.interpre.domain.interview.entity.InterviewSession;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface InterviewSessionRepository extends CrudRepository<InterviewSession, UUID> {
}
