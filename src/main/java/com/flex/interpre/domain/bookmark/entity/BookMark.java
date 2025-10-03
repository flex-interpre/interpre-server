package com.flex.interpre.domain.bookmark.entity;

import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.user.entity.JobSeeker;
import jakarta.persistence.*;
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

    @MapsId("recruitmentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id", nullable = false)
    private Recruitment recruitment;

}

