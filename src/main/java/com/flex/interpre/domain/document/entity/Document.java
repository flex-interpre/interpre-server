package com.flex.interpre.domain.document.entity;

import com.flex.interpre.domain.document.dto.request.DocumentUploadRequest;
import com.flex.interpre.domain.user.entity.JobSeeker;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name= "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    private JobSeeker jobSeeker;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "file_size", nullable = false)
    private int fileSize;

    @Column(columnDefinition = "text", nullable = false)
    private String contentText;

    @Column(name = "document_vector", columnDefinition = "float8[]")
    private List<Double> documentVector;

    /* 메서드 */

    public static Document create(JobSeeker jobSeeker, DocumentUploadRequest request,
                                  String fileUrl, String extractedText, List<Double> embedding ){
        return Document.builder()
                .jobSeeker(jobSeeker)
                .documentType(request.documentType())
                .fileName(request.file().getOriginalFilename())
                .fileUrl(fileUrl)
                .fileSize((int) request.file().getSize())
                .contentText(extractedText)
                .documentVector(embedding)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }
}
