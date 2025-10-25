package com.flex.interpre.domain.interview.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Competency {

    PROBLEM_SOLVING(
            "문제해결력"
    ),

    COMMUNICATION(
            "의사소통능력"
    ),

    TEAMWORK(
            "팀워크/협업"
    ),

    ADAPTABILITY(
            "적응력/유연성"
    ),

    INITIATIVE(
            "주도성/자기주도성"
    );

    private final String mean;
}
