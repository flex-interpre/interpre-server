package com.flex.interpre.domain.document.service;

import com.flex.interpre.domain.document.dto.request.DocumentUploadRequest;
import com.flex.interpre.domain.document.dto.response.DocumentResponse;
import com.flex.interpre.domain.document.entity.Document;
import com.flex.interpre.domain.document.repository.DocumentRepository;
import com.flex.interpre.domain.user.entity.JobSeeker;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.global.module.embedding.ClovaEmbeddingService;
import com.flex.interpre.global.module.pdf.PdfExtractor;
import com.flex.interpre.global.module.pdf.exception.PdfExtractorExceptions;
import com.flex.interpre.global.module.s3.S3DocumentUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

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
    @PreAuthorize("hasRole('JOBSEEKER')")
    public DocumentResponse uploadDocument(User user, DocumentUploadRequest request){
        JobSeeker jobSeeker = user.getJobSeeker();

        String fileUrl = s3Uploader.uploadDocument(request.file(), DIR_NAME); // s3 저장 후 url
        String extractedText = pdfExtractor.extract(request.file()); // pdf -> text로 변환
        float[] embedding = clovaEmbeddingService.embed(extractedText); // text -> embedding

        // db 저장
        Document document = Document.create(jobSeeker, request, fileUrl, extractedText, embedding);
        documentRepository.save(document);

        return DocumentResponse.from(document);
    }

}
