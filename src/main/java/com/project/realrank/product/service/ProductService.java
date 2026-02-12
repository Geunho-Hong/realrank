package com.project.realrank.product.service;

import com.project.realrank.product.domain.Product;
import com.project.realrank.product.domain.ProductCategory;
import com.project.realrank.product.dto.ProductCreateReqDto;
import com.project.realrank.product.dto.ProductCreateResDto;
import com.project.realrank.product.dto.ProductSearchResDto;
import com.project.realrank.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductCreateResDto createProduct(ProductCreateReqDto reqDto) {
        ProductCategory category = ProductCategory.findCategory(reqDto.category());
        Product product = Product.from(reqDto, category);
        productRepository.save(product);
        return ProductCreateResDto.from(product);
    }

    public ProductSearchResDto getProduct(final String productCode) {
        Product product = productRepository.getProductByProductCode(productCode);
        return ProductSearchResDto.from(product);
    }

}
