package com.flex.interpre.domain.document.service;

import com.flex.interpre.domain.document.dto.request.DocumentUploadRequest;
import com.flex.interpre.domain.document.dto.response.DocumentResponse;
import com.flex.interpre.domain.document.entity.Document;
import com.flex.interpre.domain.document.repository.DocumentRepository;
import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.jobSeeker.entity.User;
import com.flex.interpre.global.module.embedding.ClovaEmbeddingService;
import com.flex.interpre.global.module.pdf.PdfExtractor;
import com.flex.interpre.global.module.s3.S3DocumentUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final S3DocumentUploader s3Uploader;
    private final PdfExtractor pdfExtractor;
    private final ClovaEmbeddingService clovaEmbeddingService;

    private static final String DIR_NAME = "documents";


    @Transactional
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public DocumentResponse uploadDocument(User user, DocumentUploadRequest request){
        JobSeeker jobSeeker = user.getJobSeeker();

        String fileUrl = s3Uploader.uploadDocument(request.file(), DIR_NAME); // s3 저장 후 url
        String extractedText = pdfExtractor.extract(request.file()); // pdf -> text로 변환
        List<Double> embedding = clovaEmbeddingService.embed(extractedText); // text -> embedding

        // db 저장
        Document document = Document.create(jobSeeker, request, fileUrl, extractedText, embedding);
        documentRepository.save(document);

        return DocumentResponse.from(document);
    }

    public List<DocumentResponse> getDocuments(User user) {
        JobSeeker jobSeeker = user.getJobSeeker();
        if (jobSeeker == null) return List.of();

        return documentRepository.findAllByJobSeekerUserIdAndDeletedAtIsNull(jobSeeker.getUser().getId()).stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasRole('JOB_SEEKER') and #document.jobSeeker.user.id == authentication.principal.id")
    public void deleteDocument(Document document) {
        s3Uploader.deleteDocument(document.getFileUrl()); // S3에서 파일 삭제

        document.markAsDeleted(); // soft delete 처리
        log.info("문서 삭제 성공: documentId={}", document.getId());
    }
}
