package com.flex.interpre.domain.bookmark.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class BookMarkId implements Serializable {

    private UUID jobSeekerId;
    private UUID recruitmentId;
}
