package com.flex.interpre.domain.document.repository;

import com.flex.interpre.domain.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findAllByJobSeekerUserIdAndDeletedAtIsNull(UUID userId);
}
