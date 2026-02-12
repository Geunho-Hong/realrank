package com.project.realrank.product.dto;

import com.project.realrank.product.domain.Product;
import com.project.realrank.product.domain.ProductCategory;

import java.math.BigDecimal;

public record ProductCreateResDto(
        String productCode,
        String name,
        BigDecimal price,
        ProductCategory category,
        String description
) {

    public static ProductCreateResDto from(Product product) {
        return new ProductCreateResDto(
                product.getProductCode(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getDescription()
        );
    }

}
