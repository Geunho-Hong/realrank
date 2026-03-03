package com.project.realrank.product.repository;

import com.project.realrank.product.domain.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMetricsRepository extends JpaRepository<ProductMetrics, Long> {

    ProductMetrics findByProduct_ProductCodeAndStatDate(String productCode, String statDate);

}
