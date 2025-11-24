package com.flex.interpre.domain.interview.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Competency {

    PROBLEM_SOLVING(
            "문제해결능력"
    ),

    COMMUNICATION(
            "의사소통능력"
    ),

    TEAMWORK(
            "대인관계능력/협업"
    ),

    ADAPTABILITY(
            "조직이해능력/적응력"
    ),

    INITIATIVE(
            "자기개발능력/주도성"
    );

    private final String mean;
}
