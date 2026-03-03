package com.project.realrank.product.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductLikeCountUpdReqDto(
        @NotBlank String productCode,
        @NotBlank String statDate
) {
}
