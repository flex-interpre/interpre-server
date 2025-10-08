package com.flex.interpre.domain.interview.entity;

import com.flex.interpre.domain.user.entity.JobSeeker;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "title")
    private String title;

    @Column(name = "duration_second")
    private Integer durationSecond;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobSeeker_id")
    private JobSeeker jobSeeker;
}
