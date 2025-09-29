package com.flex.interpre.domain.bookmark.repository;

import com.flex.interpre.domain.bookmark.entity.BookMark;
import com.flex.interpre.domain.bookmark.entity.BookMarkId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookMarkRepository extends JpaRepository<BookMark, BookMarkId> {
}
