package com.flex.interpre.domain.recruitment.service;

import com.flex.interpre.domain.recruitment.dto.request.RecruitmentCreateUpdateRequest;
import com.flex.interpre.domain.recruitment.dto.request.RecruitmentSearchRequest;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentResponse;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentSummaryResponse;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.recruitment.repository.RecruitmentRepository;
import com.flex.interpre.domain.company.entity.Company;
import com.flex.interpre.domain.recruitment.repository.RecruitmentSpecification;
import com.flex.interpre.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;


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

    // 공고문 목록 조회 (검색 + 필터 + 정렬)
    public Page<RecruitmentSummaryResponse> searchRecruitments(RecruitmentSearchRequest request){
        // OpenSearch 검색 결과 ID
        List<UUID> matchedIds = recruitmentIndexService.searchIdsByKeyword(
                request.keyword(),
                request.excludeKeyword(),
                request.searchFields()
        );

        // DB 필터링
        Specification<Recruitment> spec = RecruitmentSpecification.filterByConditions(
                request.jobAreas(),
                request.employmentTypes(),
                request.minExperience(),
                request.maxExperience(),
                request.jobFirsts(),
                request.jobSeconds(),
                request.jobThirds()
        );

        // OpenSearch 결과 ID 필터 추가
        if (!matchedIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("id").in(matchedIds));
        }

        // 정렬 기준 지정
        Sort sort = switch (request.sort()) {
            case "popular" -> Sort.by(Sort.Direction.DESC, "viewCount");
            case "deadline" -> Sort.by(Sort.Direction.ASC, "deadline");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // 최신순
        };

        Pageable pageable = PageRequest.of(
                request.page() != null ? request.page() : 0,
                request.size() != null ? request.size() : 20,
                sort
        );

        Page<Recruitment> page = recruitmentRepository.findAll(spec, pageable);
        return page.map(RecruitmentSummaryResponse::from);
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
        recruitment.update(request); // 공고문 데이터 업데이트

        // 인덱싱/임베딩 갱신
        try {
            recruitmentIndexService.indexRecruitment(recruitment);
            log.info("공고 인덱스 갱신 완료: {}", recruitment.getId());
        } catch (Exception e) {
            log.error("공고 인덱스 갱신 실패 ({}): {}", recruitment.getId(), e.getMessage());
        }

        return RecruitmentResponse.from(recruitment);
    }

    // 공고문 삭제
    @Transactional
    @PreAuthorize("hasRole('COMPANY') and #recruitment.company.user.id == authentication.principal.id")
    public void deleteRecruitment(Recruitment recruitment) {
        recruitmentRepository.delete(recruitment); // DB에서 삭제

        try {
            recruitmentIndexService.deleteRecruitment(recruitment.getId());
            log.info("공고 인덱스 문서 삭제 완료: {}", recruitment.getId());
        } catch (Exception e) {
            log.error("공고 인덱스 문서 삭제 실패 ({}): {}", recruitment.getId(), e.getMessage());
        }
    }
}
