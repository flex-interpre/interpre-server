package com.flex.interpre.domain.document.controller;

import com.flex.interpre.domain.document.dto.request.DocumentUploadRequest;
import com.flex.interpre.domain.document.dto.response.DocumentResponse;
import com.flex.interpre.domain.document.entity.Document;
import com.flex.interpre.domain.document.service.DocumentService;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/documents")
public class DocumentController {
    private final DocumentService documentService;

    @Operation(summary = "문서 업로드 (multipart/form-data 요청)")
    @PostMapping
    public ApiResponse<DocumentResponse> uploadDocument(@AuthenticationPrincipal User user, @ModelAttribute DocumentUploadRequest request){
        return ApiResponse.ok(documentService.uploadDocument(user, request));
    }

    @Operation(summary = "문서 목록 조회")
    @GetMapping
    public ApiResponse<List<DocumentResponse>> getDocuments(@AuthenticationPrincipal User user){
        return ApiResponse.ok(documentService.getDocuments(user));
    }

    @Operation(summary = "문서 삭제")
    @DeleteMapping("/{document}")
    public ApiResponse<Void> deleteDocument(@PathVariable Document document){
        documentService.deleteDocument(document);
        return ApiResponse.ok();
    }
}
