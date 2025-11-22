package com.flex.interpre.domain.recruitment.service;

import com.flex.interpre.domain.company.entity.Company;
import com.flex.interpre.domain.company.repository.CompanyRepository;
import com.flex.interpre.domain.recruitment.dto.request.RecruitmentImportRequest;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.recruitment.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentImportService {

    private final RecruitmentRepository recruitmentRepository;
    private final CompanyRepository companyRepository;
    private final RecruitmentIndexService recruitmentIndexService;

    // 공고문 대량 임포트 및 임베딩 자동 생성
    @Transactional
    public int importRecruitments(List<RecruitmentImportRequest> requests) {
        int count = 0;

        for (RecruitmentImportRequest request : requests) {
            try {
                importOneRecruitment(request);
                count++;
            } catch (Exception e) {
                log.error("공고문 임포트 실패 ({}): {}", request.title(), e.getMessage());
            }
        }

        log.info("총 {}건 임포트 완료", count);
        return count;
    }

    // 개별 공고문 처리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void importOneRecruitment(RecruitmentImportRequest request) {
        // 회사명 기준으로 Company upsert
        Company company = companyRepository.findByCompanyName(request.companyName())
                .orElseGet(() -> {
                    try {
                        return companyRepository.save(
                                Company.builder()
                                        .companyName(request.companyName())
                                        .build()
                        );
                    } catch (DataIntegrityViolationException e) {
                        // 동시에 같은 이름 insert 시 race condition 방지
                        return companyRepository.findByCompanyName(request.companyName())
                                .orElseThrow(() -> new RuntimeException("Company race condition"));
                    }
                });

        // 공고문 생성
        Recruitment recruitment = Recruitment.createFromImport(request, company);
        recruitmentRepository.save(recruitment);
    }

    // 공고문 db 데이터 인덱싱
    @Transactional(readOnly = true)
    public void indexAllToOpenSearch() {
        try {
            List<Recruitment> recruitments = recruitmentRepository.findAll();
            if (recruitments.isEmpty()) {
                log.warn("인덱싱할 공고문이 없습니다.");
                return;
            }

            log.info("Bulk 인덱싱 시작... 총 {}건", recruitments.size());
            recruitmentIndexService.bulkIndexRecruitments(recruitments);

            log.info("Bulk 인덱싱 완료!");

        } catch (Exception e) {
            log.error("Bulk 인덱싱 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
