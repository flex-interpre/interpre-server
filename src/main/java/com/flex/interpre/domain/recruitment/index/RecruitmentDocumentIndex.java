package com.flex.interpre.domain.recruitment.index;

import lombok.*;
import org.springframework.data.annotation.Id;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentDocumentIndex {

    private UUID id;           // 공고문 PK
    private CompanyInfo company;
//    private UUID companyId;    // 회사 PK
//    private String companyName;

    private String title;
    private String description;
    private String location;

    private List<String> jobAreas;
    private List<String> jobFirsts;
    private List<String> jobSeconds;
    private List<String> jobThirds;
    private List<String> employmentTypes;
    private List<String> requirements;
    private List<String> benefits;
    private List<String> skills;

    private List<Double> embedding;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfo {
        private UUID id;
        private String companyName;
    }
}

