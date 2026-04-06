package com.project.realrank.product.service;

import com.project.realrank.common.lock.DistributedLock;
import com.project.realrank.product.domain.Product;
import com.project.realrank.product.domain.ProductCategory;
import com.project.realrank.product.domain.ProductMetrics;
import com.project.realrank.product.dto.*;
import com.project.realrank.product.repository.ProductMetricsRepository;
import com.project.realrank.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMetricsRepository productMetricsRepository;

    @Transactional
    public ProductCreateResDto createProduct(ProductCreateReqDto reqDto) {
        ProductCategory category = ProductCategory.findCategory(reqDto.category());
        Product product = Product.from(reqDto, category);
        productRepository.save(product);
        return ProductCreateResDto.from(product);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "product:detail", key = "#productCode", sync = true)
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
    @CacheEvict(cacheNames = "product:detail", key = "#productUpdReqDto.productCode()")
    public boolean updateProduct(ProductUpdReqDto productUpdReqDto) {
        Product product = productRepository.getProductByProductCode(productUpdReqDto.productCode())
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));
        product.updateProduct(productUpdReqDto);
        return true;
    }

    @Transactional
    @CacheEvict(cacheNames = "product:detail", key = "#productCode")
    public boolean deleteProduct(String productCode) {
        Product product = productRepository.getProductByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));
        product.deactivateProduct();
        return true;
    }

    @Transactional
    public boolean increaseViewCount(ProductViewCountUpdReqDto productViewCountUpdReqDto) {
        final String productCode = productViewCountUpdReqDto.productCode();
        final String statDate = productViewCountUpdReqDto.statDate();
        Product product = productRepository.getProductByProductCode(productCode)
                    .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다"));
        ProductMetrics productMetrics = productMetricsRepository.findByProductCodeAndStatDate(productCode, statDate);
        if (ObjectUtils.isEmpty(productMetrics)) {
            productMetricsRepository.save(ProductMetrics.from(statDate, product));
        } else {
            productMetrics.increaseView();
        }
        return true;
    }

    @Transactional(readOnly = true)
    public ProductLikeCountResDto getLikeCount(ProductLikeCountReqDto reqDto) {
        final String productCode = reqDto.productCode();
        final String statDate = reqDto.statDate();
        productRepository.getProductByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다"));

        if (StringUtils.hasText(statDate)) {
            ProductMetrics metrics = productMetricsRepository.findByProductCodeAndStatDate(productCode, statDate);
            long count = (metrics != null) ? metrics.getLikeCount() : 0L;
            return ProductLikeCountResDto.of(productCode, statDate, count);
        }

        long totalCount = productMetricsRepository.sumLikeCountByProductCode(productCode);
        return ProductLikeCountResDto.of(productCode, null, totalCount);
    }


    @DistributedLock(key = "likeLock")
    public boolean increaseLikeCount(ProductLikeCountUpdReqDto productLikeCountUpdReqDto) {
        final String productCode = productLikeCountUpdReqDto.productCode();
        final String statDate = productLikeCountUpdReqDto.statDate();

        log.info("productCode = {} ", productCode);
        log.info("statDate = {} ", statDate);

        Product product = productRepository.getProductByProductCode(productCode)
                    .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다"));
        ProductMetrics productMetrics = productMetricsRepository.findByProductCodeAndStatDate(productCode, statDate);

        if (ObjectUtils.isEmpty(productMetrics)) {
            productMetricsRepository.save(ProductMetrics.from(statDate, product));
        } else {
            productMetrics.increaseLike();
        }
        return true;
    }

}
