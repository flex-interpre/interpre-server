package com.flex.interpre.domain.bookmark.entity;

import com.flex.interpre.domain.user.entity.JobSeeker;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class BookMark {

    @EmbeddedId
    private BookMarkId id;

    @MapsId("jobSeekerId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    private JobSeeker jobSeeker;

    //공고 엔티티 추가 후 매핑
//    @MapsId("recruitmentId")
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "recruitment_id", nullable = false)
//    private Recruitment recruitment;

}

