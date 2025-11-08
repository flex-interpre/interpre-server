//package com.flex.interpre.domain.user.dto.response;
//
//import com.fasterxml.jackson.annotation.JsonSubTypes;
//import com.fasterxml.jackson.annotation.JsonTypeInfo;
//
//@JsonTypeInfo(
//        use = JsonTypeInfo.Id.NAME,
//        include = JsonTypeInfo.As.PROPERTY, // 식별자를 JSON 객체 속성으로 포함
//        property = "role" // 필드명 지정
//)
//@JsonSubTypes({
//        @JsonSubTypes.Type(value = MyJobSeekerInfo.class, name = "jobSeeker"),
//        @JsonSubTypes.Type(value = MyCompanyInfo.class, name = "company")
//})
//public interface MyUserDetailInfo {
//}