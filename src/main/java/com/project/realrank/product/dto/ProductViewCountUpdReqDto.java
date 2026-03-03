package com.project.realrank.product.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductViewCountUpdReqDto(
        @NotBlank String productCode,
        @NotBlank String statDate
) {
}
