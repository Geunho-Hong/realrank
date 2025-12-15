package com.project.realrank.product.repository;

import com.project.realrank.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    
}
