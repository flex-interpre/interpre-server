package com.flex.interpre.domain.recruitment.controller;

import com.flex.interpre.domain.recruitment.dto.request.RecruitmentImportRequest;
import com.flex.interpre.domain.recruitment.service.RecruitmentImportService;
import com.flex.interpre.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recruitments")
@RequiredArgsConstructor
public class RecruitmentImportController {

    private final RecruitmentImportService recruitmentImportService;

    @Operation(summary = "대량 공고문 임포트 및 임베딩 자동 생성")
    @PostMapping("/import")
    public ApiResponse<Integer> importRecruitments(@RequestBody List<RecruitmentImportRequest> requests) {
        int count = recruitmentImportService.importRecruitments(requests);
        return ApiResponse.ok(count);
    }

}
