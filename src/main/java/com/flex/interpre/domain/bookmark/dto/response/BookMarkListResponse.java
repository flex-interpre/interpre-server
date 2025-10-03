package com.flex.interpre.domain.bookmark.dto.response;

import com.flex.interpre.domain.bookmark.entity.BookMark;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor(staticName = "of")
@Schema(description = "북마크 목록 응답")
public class BookMarkListResponse {

    List<BookMarkResponse> bookmarks;

    public static BookMarkListResponse from(Set<BookMark> bookmarks) {

        return BookMarkListResponse.of(bookmarks.stream().map(BookMarkResponse::from).toList());
    }
}
