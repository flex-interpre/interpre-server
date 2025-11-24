package com.flex.interpre.domain.jobSeeker.service;

import com.flex.interpre.domain.jobSeeker.entity.Bookmark;
import com.flex.interpre.domain.jobSeeker.entity.JobSeeker;
import com.flex.interpre.domain.jobSeeker.repository.BookmarkRepository;
import com.flex.interpre.domain.jobSeeker.repository.JobSeekerRepository;
import com.flex.interpre.domain.recruitment.dto.response.RecruitmentSummaryResponse;
import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.recruitment.exception.RecruitmentExceptions;
import com.flex.interpre.domain.recruitment.repository.RecruitmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final RecruitmentRepository recruitmentRepository;

    // 북마크 추가
    @Transactional
    public void addBookmark(JobSeeker jobSeeker, UUID recruitmentId) {
        Recruitment recruitment = recruitmentRepository.findById(recruitmentId)
                .orElseThrow(RecruitmentExceptions.RECRUITMENT_NOT_FOUND::toException);

        Bookmark bookmark = bookmarkRepository.findByJobSeekerIdAndRecruitmentId(jobSeeker.getId(), recruitmentId)
                .orElseGet(() -> Bookmark.builder()
                        .jobSeeker(jobSeeker)
                        .recruitment(recruitment)
                        .liked(true)
                        .build()
                );
        bookmark.setLiked(true);
        bookmarkRepository.save(bookmark);
    }

    // 북마크 삭제
    public void removeBookmark(JobSeeker jobSeeker, UUID recruitmentId) {
        Bookmark bookmark = bookmarkRepository.findByJobSeekerIdAndRecruitmentId(jobSeeker.getId(), recruitmentId)
                .orElseThrow(() -> new EntityNotFoundException("북마크가 존재하지 않습니다."));

        bookmark.setLiked(false);
        bookmarkRepository.save(bookmark);
    }

    // 북마크 공고 목록 조회
    public List<RecruitmentSummaryResponse> getBookmarks(JobSeeker jobSeeker) {
        return bookmarkRepository.findAllByJobSeekerId(jobSeeker.getId())
                .stream()
                .filter(Bookmark::isLiked)
                .map(Bookmark::getRecruitment)
                .map(RecruitmentSummaryResponse::from)
                .toList();
    }
}
