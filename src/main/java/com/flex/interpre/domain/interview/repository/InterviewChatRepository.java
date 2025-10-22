package com.flex.interpre.domain.interview.repository;

import com.flex.interpre.domain.interview.entity.InterviewChat;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface InterviewChatRepository extends CrudRepository<InterviewChat, UUID> {
}
