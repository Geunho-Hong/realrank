package com.project.realrank.product.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ProductCreateReqDto(
        @NotBlank String productCode,
        String name,
        BigDecimal price,
        @NotBlank String category,
        String description
) {
}
