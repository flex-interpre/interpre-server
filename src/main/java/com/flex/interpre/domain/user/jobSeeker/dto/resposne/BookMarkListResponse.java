package com.flex.interpre.domain.user.jobSeeker.dto.resposne;

import com.flex.interpre.domain.recruitment.dto.response.RecruitmentSummaryResponse;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor(staticName = "of")
@Schema(description = "북마크 목록 응답")
public class BookMarkListResponse {

    List<RecruitmentSummaryResponse> bookmarks;

    public static BookMarkListResponse from(Set<Recruitment> recruitments) {

        return BookMarkListResponse.of(recruitments.stream().map(RecruitmentSummaryResponse::from).toList());
    }
}
