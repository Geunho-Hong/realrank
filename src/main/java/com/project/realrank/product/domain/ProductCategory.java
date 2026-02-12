package com.project.realrank.product.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum ProductCategory {

    BOOK("BOOK"),
    STATIONARY("STATIONARY");

    private final String code;

    ProductCategory(String code) {
        this.code = code;
    }

    private static final Map<String, ProductCategory> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(ProductCategory::getCode, e -> e));

    public static ProductCategory findCategory(String keyWord) {
        return CODE_MAP.get(keyWord);
    }

}
