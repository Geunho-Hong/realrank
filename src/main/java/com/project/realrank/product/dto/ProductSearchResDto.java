package com.project.realrank.product.dto;

import com.project.realrank.product.domain.Product;
import com.project.realrank.product.domain.ProductCategory;

import java.math.BigDecimal;

public record ProductSearchResDto(
        String productCode,
        String name,
        BigDecimal price,
        ProductCategory category,
        String description
) {

    public static ProductSearchResDto from(Product product) {
        return new ProductSearchResDto(
                product.getProductCode(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getDescription()
        );
    }

}
