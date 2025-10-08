package com.flex.interpre.domain.document.dto.response;

import com.flex.interpre.domain.document.entity.Document;
import com.flex.interpre.domain.document.entity.DocumentType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record DocumentResponse(
        UUID id,
        DocumentType documentType,
        String fileName,
        String fileUrl,
        int fileSize,
        LocalDateTime createdAt
) {
    public static DocumentResponse from(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .fileSize(document.getFileSize())
                .createdAt(document.getCreatedAt())
                .build();
    }
}
