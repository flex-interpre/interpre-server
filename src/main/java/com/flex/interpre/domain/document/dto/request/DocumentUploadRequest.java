package com.flex.interpre.domain.document.dto.request;

import com.flex.interpre.domain.document.entity.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public record DocumentUploadRequest(
        @Schema(description = "문서 타입 (RESUME, COVER_LETTER, PORTFOLIO, OTHER)")
        DocumentType documentType,

        @Schema(description = "업로드할 PDF 파일")
        MultipartFile file
){}
