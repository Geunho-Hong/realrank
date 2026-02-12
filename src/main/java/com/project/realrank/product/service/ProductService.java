package com.project.realrank.product.service;

import com.project.realrank.product.domain.Product;
import com.project.realrank.product.domain.ProductCategory;
import com.project.realrank.product.dto.ProductCreateReqDto;
import com.project.realrank.product.dto.ProductCreateResDto;
import com.project.realrank.product.dto.ProductSearchResDto;
import com.project.realrank.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public ProductSearchResDto getProduct(final String productCode) {
        Product product = productRepository.getProductByProductCode(productCode);
        return ProductSearchResDto.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductSearchResDto> getProductsByName(String name) {
        return productRepository.getProductsByNameLike(name + "%")
                .stream()
                .map(ProductSearchResDto::from)
                .toList();
    }


}
