package com.flex.interpre.domain.user.jobSeeker.dto.resposne;

import com.flex.interpre.domain.recruitment.entity.JobGroup;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.user.dto.response.SimpleCompanyResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor(staticName = "of")
@Schema(description = "북마크 응답")
public class BookMarkResponse {

    private UUID id;

    private SimpleCompanyResponse company;

    private String title;

    private Set<JobGroup> jobGroups;

    private LocalDateTime deadline;

    private int viewCount;

    public static BookMarkResponse from(Recruitment recruitment) {

        SimpleCompanyResponse response = SimpleCompanyResponse.from(recruitment.getCompany());

        return BookMarkResponse.of(recruitment.getId(), response, recruitment.getTitle(), recruitment.getJobGroups(), recruitment.getDeadline(), recruitment.getViewCount());
    }
}


