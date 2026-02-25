package com.project.realrank.product.service;

import com.project.realrank.product.domain.Product;
import com.project.realrank.product.domain.ProductCategory;
import com.project.realrank.product.dto.ProductCreateReqDto;
import com.project.realrank.product.dto.ProductCreateResDto;
import com.project.realrank.product.dto.ProductSearchResDto;
import com.project.realrank.product.dto.ProductUpdReqDto;
import com.project.realrank.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        Product product = productRepository.getProductByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));
        return ProductSearchResDto.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductSearchResDto> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findAll(pageable);
        return products.stream()
                .map(ProductSearchResDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductSearchResDto> getProductsByName(String name) {
        return productRepository.getProductsByNameLike(name + "%")
                .stream()
                .map(ProductSearchResDto::from)
                .toList();
    }

    @Transactional
    public boolean updateProduct(ProductUpdReqDto productUpdReqDto) {
        Product product = productRepository.getProductByProductCode(productUpdReqDto.productCode())
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));
        product.updateProduct(productUpdReqDto);
        return true;
    }

    @Transactional
    public boolean deleteProduct(String productCode) {
        Product product = productRepository.getProductByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));
        product.deactivateProduct();
        return true;
    }


}
