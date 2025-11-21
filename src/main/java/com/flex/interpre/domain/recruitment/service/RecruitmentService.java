package com.flex.interpre.domain.recruitment.service;

import com.flex.interpre.domain.company.entity.Company;
import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.recruitment.dto.request.RecruitmentCreateUpdateRequest;
import com.flex.interpre.domain.recruitment.dto.request.RecruitmentSearchRequest;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentResponse;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentSummaryResponse;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.recruitment.exception.RecruitmentExceptions;
import com.flex.interpre.domain.recruitment.repository.RecruitmentRepository;
import com.flex.interpre.domain.recruitment.repository.RecruitmentSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
    public RecruitmentResponse createRecruitment(Company company, RecruitmentCreateUpdateRequest request) {
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
    
    // 공고문 추천 조회 (로그인 된 유저용)
    public Page<RecruitmentSummaryResponse> getRecommendedRecruitments(JobSeeker jobSeeker, Pageable pageable) {
        
        if (!jobSeeker.getCumulativeEmbedding().isEmpty()) {
            try {
                List<Recruitment> sortedRecruitments = recruitmentIndexService.searchByVector(
                        jobSeeker.getCumulativeEmbedding(),
                        pageable.getPageSize()
                );
                
                if (!sortedRecruitments.isEmpty()) {
                    List<RecruitmentSummaryResponse> responses = sortedRecruitments.stream()
                            .map(RecruitmentSummaryResponse::from)
                            .toList();
                    
                    return new PageImpl<>(responses, pageable, responses.size());
                }
            } catch (IOException e) {
                log.warn("벡터 검색 오류 발생 {}", e.getMessage());
            }
        }
        
        Page<Recruitment> recruitmentPage = recruitmentRepository.findAllActive(pageable);
        return recruitmentPage.map(RecruitmentSummaryResponse::from);
    }
    
    // 공고문 목록 조회 (검색 + 필터 + 정렬)
    public Page<RecruitmentSummaryResponse> searchRecruitments(RecruitmentSearchRequest request) {
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
        String sortType = request.sort() == null ? "recent" : request.sort(); // null 방지
        
        Sort sort = switch (sortType) {
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
    public RecruitmentResponse getRecruitment(UUID recruitmentId) {
        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
                .orElseThrow(RecruitmentExceptions.RECRUITMENT_NOT_FOUND::toException);
        recruitment.increaseViewCount(); // 조회 시 조회수 증가
        
        return RecruitmentResponse.from(recruitment);
    }
    
    // 공고문 업데이트
    @Transactional
    public RecruitmentResponse updateRecruitment(Company company, UUID recruitmentId, RecruitmentCreateUpdateRequest request) {
        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
                .orElseThrow(RecruitmentExceptions.RECRUITMENT_NOT_FOUND::toException);
        
        // 소유권 검증 (간소화)
        if (!recruitment.getCompany().getId().equals(company.getId())) {
            throw RecruitmentExceptions.ACCESS_DENIED.toException();
        }
        
        recruitment.update(request); // 공고문 데이터 업데이트
        
        // 인덱싱/임베딩 갱신
        try {
            recruitmentIndexService.indexRecruitment(recruitment);
            log.info("공고 인덱스 갱신 완료: {}", recruitment.getId());
        } catch (Exception e) {
            log.warn("공고 인덱스 갱신 실패 ({}): {}", recruitment.getId(), e.getMessage());
        }
        
        return RecruitmentResponse.from(recruitment);
    }
    
    // 공고문 삭제
    @Transactional
    public void deleteRecruitment(Company company, UUID recruitmentId) {
        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
                .orElseThrow(RecruitmentExceptions.RECRUITMENT_NOT_FOUND::toException);
        
        // 소유권 검증 (간소화)
        if (!recruitment.getCompany().getId().equals(company.getId())) {
            throw RecruitmentExceptions.ACCESS_DENIED.toException();
        }
        
        recruitmentRepository.delete(recruitment); // DB에서 삭제
        
        try {
            recruitmentIndexService.deleteRecruitment(recruitment.getId());
            log.info("공고 인덱스 문서 삭제 완료: {}", recruitment.getId());
        } catch (Exception e) {
            log.error("공고 인덱스 문서 삭제 실패 ({}): {}", recruitment.getId(), e.getMessage());
        }
    }
}