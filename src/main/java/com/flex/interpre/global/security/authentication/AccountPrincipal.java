package com.flex.interpre.global.security.authentication;


import com.flex.interpre.global.constant.Role;

import java.util.UUID;

// JobSeeker와 Company의 공통 인증 정보를 제공
public interface AccountPrincipal {
    UUID getId();
    String getEmail();
    String getGoogleId();
    Role getRole();
    boolean isApproved();

    // 구직자 검증 메서드
    default boolean isJobSeeker() {
        return getRole() == Role.JOB_SEEKER;
    }

    // 기업 검증 메서드
    default boolean isCompany() {
        return getRole() == Role.COMPANY;
    }
}