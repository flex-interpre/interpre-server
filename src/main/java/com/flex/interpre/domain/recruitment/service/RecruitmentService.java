package com.flex.interpre.domain.recruitment.service;

import com.flex.interpre.domain.recruitment.dto.request.RecruitmentCreateUpdateRequest;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentResponse;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentSummaryResponse;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.recruitment.exception.RecruitmentExceptions;
import com.flex.interpre.domain.recruitment.repository.RecruitmentRepository;
import com.flex.interpre.domain.user.entity.Company;
import com.flex.interpre.domain.user.entity.Role;
import com.flex.interpre.domain.user.entity.User;
import com.flex.interpre.domain.user.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentService {
    private final RecruitmentRepository recruitmentRepository;
    private final CompanyRepository companyRepository;

    // 공고문 생성
    @Transactional
    public RecruitmentResponse createRecruitment(User user, RecruitmentCreateUpdateRequest request) {
        validateCompanyRole(user); // 기업인지 확인
        Company company = companyRepository.findById(user.getId()) // 기업 조회
                .orElseThrow(RecruitmentExceptions.COMPANY_NOT_FOUND::toException);

        Recruitment recruitment = Recruitment.create(request, company); // 공고문 생성
        recruitmentRepository.save(recruitment);

        return RecruitmentResponse.from(recruitment);
    }

    // 공고문 전체 조회
    @Transactional(readOnly = true)
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
    public RecruitmentResponse updateRecruitment(User user, Recruitment recruitment, RecruitmentCreateUpdateRequest request) {
        validateCompanyRole(user);

        checkRecruitmentOwner(recruitment, user); // 본인 회사가 등록한 공고인지 확인
        recruitment.update(request); // 공고문 업데이트

        return RecruitmentResponse.from(recruitment);
    }

    // 공고문 삭제
    @Transactional
    public void deleteRecruitment(User user, Recruitment recruitment) {
        validateCompanyRole(user);

        checkRecruitmentOwner(recruitment, user);

        recruitmentRepository.delete(recruitment);
    }

    /*  내부 메서드  */

    // 유저가 기업 Role인지 검증
    private void validateCompanyRole(User user) {
        if (user.getRole() != Role.COMPANY) {
            throw RecruitmentExceptions.INVALID_ROLE.toException();
        }
    }

    // 해당 공고문이 본인 기업의 것인지 확인
    private void checkRecruitmentOwner(Recruitment recruitment, User user){
        if (!recruitment.getCompany().getUser().getId().equals(user.getId())) {
            throw RecruitmentExceptions.ACCESS_DENIED.toException();
        }
    }
}
