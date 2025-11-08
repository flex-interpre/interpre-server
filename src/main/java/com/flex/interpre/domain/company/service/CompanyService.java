package com.flex.interpre.domain.company.service;

import com.flex.interpre.domain.company.dto.CompanyDetailResponse;
import com.flex.interpre.domain.company.dto.CompanySummaryResponse;
import com.flex.interpre.domain.company.entity.Company;
import com.flex.interpre.domain.recruitment.exception.RecruitmentExceptions;
import com.flex.interpre.domain.user.dto.request.UpdateMyCompanyInfo;
import com.flex.interpre.domain.user.dto.response.MyCompanyInfo;
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
public class CompanyService {
    private final CompanyRepository companyRepository;

    @Transactional
    public MyCompanyInfo updateMyInfo(Company company, UpdateMyCompanyInfo request) {
        company.update(request);

        return MyCompanyInfo.from(company);
    }

    public Page<CompanySummaryResponse> getCompanies(Pageable pageable) {
        Page<Company> companies = companyRepository.findAll(pageable);
        return companies.map(CompanySummaryResponse::from);
    }


    public CompanyDetailResponse getCompanyDetails(UUID companyId) {
        Company company = companyRepository.findByIdWithActiveRecruitments(companyId)
                .orElseThrow(RecruitmentExceptions.COMPANY_NOT_FOUND::toException);

        return CompanyDetailResponse.from(company);
    }
}
