package com.flex.interpre.domain.document.controller;

import com.flex.interpre.domain.document.dto.request.DocumentUploadRequest;
import com.flex.interpre.domain.document.dto.response.DocumentResponse;
import com.flex.interpre.domain.document.service.DocumentService;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/documents")
public class DocumentController {
    private final DocumentService documentService;

    // 문서 업로드 (multipart/form-data 요청)
    @PostMapping
    public ApiResponse<DocumentResponse> uploadDocument(@AuthenticationPrincipal User user, @ModelAttribute DocumentUploadRequest request) throws IOException {
        return ApiResponse.ok(documentService.uploadDocument(user, request));
    }


    // 문서 목록 조회
//    @GetMapping
//    public ApiResponse<List<DocumentResponse>> getDocuments(@AuthenticationPrincipal User user){
//        return ApiResponse.ok();
//    }

    // 문서 삭제
//    @DeleteMapping("/{documentId}")
//    public ApiResponse<Void> deleteDocument(@AuthenticationPrincipal User user, @PathVariable UUID documentId){
//    return ApiResponse.ok();
//    }
}
