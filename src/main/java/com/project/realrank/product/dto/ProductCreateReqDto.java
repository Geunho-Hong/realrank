package com.project.realrank.product.dto;

import java.math.BigDecimal;

public record ProductCreateReqDto(
        String productCode,
        String name,
        BigDecimal price,
        String category,
        String description
) {
}
