package com.flex.interpre.domain.recruitment.service;

import com.flex.interpre.domain.recruitment.dto.request.RecruitmentCreateUpdateRequest;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentResponse;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentSummaryResponse;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.recruitment.repository.RecruitmentRepository;
import com.flex.interpre.domain.company.entity.Company;
import com.flex.interpre.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentService {
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentIndexService recruitmentIndexService;

    // 공고문 생성
    @Transactional
    @PreAuthorize("hasRole('COMPANY')")
    public RecruitmentResponse createRecruitment(User user, RecruitmentCreateUpdateRequest request) {
        Company company = user.getCompany(); // 유저로부터 해당 기업 조회

        Recruitment recruitment = Recruitment.create(request, company); // 공고문 생성
        recruitmentRepository.save(recruitment); // DB 저장

        try { // 공고문 인덱싱
            recruitmentIndexService.indexRecruitment(recruitment);
            log.info("공고 인덱싱 완료: {}", recruitment.getId());
        } catch (Exception e) {
            log.error("공고 인덱싱 실패 ({}): {}", recruitment.getId(), e.getMessage());
        }

        return RecruitmentResponse.from(recruitment);
    }

    // 공고문 전체 조회
    public Page<RecruitmentSummaryResponse> getAllRecruitments(Pageable pageable) {
        Page<Recruitment> recruitmentPage = recruitmentRepository.findAllActive(pageable);

        return recruitmentPage.map(RecruitmentSummaryResponse::from);
    }

    // 공고문 상세 조회
    @Transactional
    public RecruitmentResponse getRecruitment(Recruitment recruitment) {
        recruitment.increaseViewCount(); // 조회 시 조회수 증가

        return RecruitmentResponse.from(recruitment);
    }

    // 공고문 업데이트
    @Transactional
    @PreAuthorize("hasRole('COMPANY') and #recruitment.company.user.id == authentication.principal.id")
    public RecruitmentResponse updateRecruitment(Recruitment recruitment, RecruitmentCreateUpdateRequest request) {
        recruitment.update(request); // 공고문 업데이트

        return RecruitmentResponse.from(recruitment);
    }

    // 공고문 삭제
    @Transactional
    @PreAuthorize("hasRole('COMPANY') and #recruitment.company.user.id == authentication.principal.id")
    public void deleteRecruitment(Recruitment recruitment) {

        recruitmentRepository.delete(recruitment);
    }
}
