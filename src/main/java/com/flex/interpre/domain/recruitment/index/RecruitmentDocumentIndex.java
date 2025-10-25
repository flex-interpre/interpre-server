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

    @Id
    private UUID id;

    private String title;
    private String description;
    private String companyName;
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
}
