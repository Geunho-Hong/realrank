package com.project.realrank.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.math.BigDecimal;

public record ProductUpdReqDto(
        @NotBlank String productCode,
        String name,
        BigDecimal price,
        String category,
        String description
) {

}
