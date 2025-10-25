package com.flex.interpre.domain.interview.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClovaSttResponse(
        @JsonProperty("text") String text
) {
}
