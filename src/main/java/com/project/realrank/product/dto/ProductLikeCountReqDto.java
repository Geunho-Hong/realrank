package com.project.realrank.product.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductLikeCountReqDto(
        @NotBlank String productCode,
        String statDate
) {
}
