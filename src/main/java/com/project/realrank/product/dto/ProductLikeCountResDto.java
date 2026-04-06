package com.project.realrank.product.dto;

public record ProductLikeCountResDto(
        String productCode,
        String statDate,
        long likeCount
) {
    public static ProductLikeCountResDto of(String productCode, String statDate, long likeCount) {
        return new ProductLikeCountResDto(productCode, statDate, likeCount);
    }
}
