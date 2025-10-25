package com.flex.interpre.domain.recruitment.service;

import com.flex.interpre.domain.company.entity.Company;
import com.flex.interpre.domain.user.repository.CompanyRepository;
import com.flex.interpre.domain.recruitment.dto.request.RecruitmentImportRequest;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.recruitment.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentImportService {

    private final RecruitmentRepository recruitmentRepository;
    private final CompanyRepository companyRepository;
    private final RecruitmentIndexService recruitmentIndexService;

    // 공고문 임포트 + 임베딩 자동 생성
    @Transactional
    public int importRecruitments(List<RecruitmentImportRequest> requests) {
        int count = 0;

        for (RecruitmentImportRequest request : requests) {
            try {
                // 회사명으로 Company 찾거나 생성
                Company company = companyRepository.findByName(request.companyName())
                        .orElseGet(() -> companyRepository.save(
                                Company.builder()
                                        .companyName(request.companyName())
                                        .build()
                        ));

                // Recruitment 생성 및 저장
                Recruitment recruitment = Recruitment.createFromImport(request, company);
                recruitmentRepository.save(recruitment);

                // 인덱싱/임베딩
                try {
                    recruitmentIndexService.indexRecruitment(recruitment);
                    log.info("임베딩 완료: {}", recruitment.getTitle());
                } catch (Exception e) {
                    log.error("인덱싱 실패 ({}): {}", recruitment.getTitle(), e.getMessage());
                }

                count++;

            } catch (Exception e) {
                log.error("임포트 실패: {}", e.getMessage());
            }
        }

        return count;
    }
}
