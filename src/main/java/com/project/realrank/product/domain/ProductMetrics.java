package com.project.realrank.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_product_metrics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime statDate;

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

}
