package com.project.realrank.product.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "tbl_product_metrics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String statDate;

    private long viewCount;

    private long likeCount;

    private LocalDateTime lastViewedAt;

    private LocalDateTime lastLikedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code")
    private Product product;

    public void increaseView() {
        this.viewCount++;
        this.lastViewedAt = LocalDateTime.now();
    }

    public void increaseLike() {
        this.likeCount++;
        this.lastLikedAt = LocalDateTime.now();
    }

    public static ProductMetrics from(String statDate, Product product) {
        return ProductMetrics.builder()
                .statDate(statDate)
                .viewCount(0)
                .likeCount(0)
                .lastViewedAt(LocalDateTime.now())
                .lastLikedAt(LocalDateTime.now())
                .product(product)
                .build();
    }

}
