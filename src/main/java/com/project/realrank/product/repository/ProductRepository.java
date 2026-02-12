package com.project.realrank.product.repository;

import com.project.realrank.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> getProductByProductCode(String productCode);

    List<Product> getProductsByNameLike(String name);

}
