package com.flex.interpre.global.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Area {
    SEOUL("서울"),
    GYEONGGI("경기"),
    INCHEON("인천"),
    GANGWON("강원"),
    CHUNGBUK("충북"),
    CHUNGNAM("충남"),
    JEONBUK("전북"),
    JEONNAM("전남"),
    GYEONGBUK("경북"),
    GYEONGNAM("경남"),
    DAEJEON("대전"),
    DAEGU("대구"),
    GWANGJU("광주"),
    BUSAN("부산"),
    ULSAN("울산"),
    SEJONG("세종"),
    JEJU("제주");

    private final String koreanName;
}
