package com.flex.interpre.domain.bookmark.dto.response;

import com.flex.interpre.domain.bookmark.entity.BookMark;
import com.flex.interpre.domain.bookmark.entity.BookMarkId;
import com.flex.interpre.domain.recruitment.entity.JobGroup;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.user.dto.response.SimpleCompanyResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor(staticName = "of")
@Schema(description = "북마크 응답")
public class BookMarkResponse {

    private BookMarkId id;

    private SimpleCompanyResponse company;

    private String title;

    private Set<JobGroup> jobGroups;

    private LocalDateTime deadline;

    private int viewCount;

    public static BookMarkResponse from(BookMark bookmark) {

        Recruitment recruitment = bookmark.getRecruitment();
        SimpleCompanyResponse response = SimpleCompanyResponse.from(recruitment.getCompany());

        return BookMarkResponse.of(bookmark.getId(), response, recruitment.getTitle(), recruitment.getJobGroups(), recruitment.getDeadline(), recruitment.getViewCount());
    }
}

