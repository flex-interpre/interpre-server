package com.flex.interpre.domain.interview.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ModifyTitleRequest(
        @NotBlank String title
) {
}
