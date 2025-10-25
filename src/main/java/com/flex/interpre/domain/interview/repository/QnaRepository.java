package com.flex.interpre.domain.interview.repository;

import com.flex.interpre.domain.interview.entity.Qna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QnaRepository extends JpaRepository<Qna, UUID> {
}
