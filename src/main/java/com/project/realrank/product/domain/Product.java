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

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    private String description;

    public Product(String productCode, String name, BigDecimal price, ProductCategory category, ProductStatus status, String description) {
        this.productCode = productCode;
        this.name = name;
        this.price = price;
        this.category = category;
        this.status = status;
        this.description = description;
    }

    public static Product from(ProductCreateReqDto productCreateReqDto, ProductCategory category) {
        return new Product(
                productCreateReqDto.productCode(),
                productCreateReqDto.name(),
                productCreateReqDto.price(),
                category,
                ProductStatus.ACTIVE,
                productCreateReqDto.description()
        );
    }

}
