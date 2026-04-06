package com.project.realrank.product.repository;

import com.project.realrank.product.domain.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductMetricsRepository extends JpaRepository<ProductMetrics, Long> {

    @Query("SELECT m FROM ProductMetrics m WHERE m.product.productCode = :productCode AND m.statDate = :statDate")
    ProductMetrics findByProductCodeAndStatDate(@Param("productCode") String productCode, @Param("statDate") String statDate);

    @Query("SELECT COALESCE(SUM(m.likeCount), 0) FROM ProductMetrics m WHERE m.product.productCode = :productCode")
    long sumLikeCountByProductCode(@Param("productCode") String productCode);

}
