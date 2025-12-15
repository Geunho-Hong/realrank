package com.project.realrank.product.domain;

import com.project.realrank.product.dto.ProductCreateReqDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "tbl_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "product_code")
    private String productCode;

    private String name;

    private BigDecimal price;

    private String category;

    private String description;

    public Product(String productCode, String name, BigDecimal price, String category, String description) {
        this.productCode = productCode;
        this.name = name;
        this.price = price;
        this.category = category;
        this.description = description;
    }

    public static Product from(ProductCreateReqDto productCreateReqDto) {
        return new Product(
                productCreateReqDto.productCode(),
                productCreateReqDto.name(),
                productCreateReqDto.price(),
                productCreateReqDto.category(),
                productCreateReqDto.description()
        );
    }

}
