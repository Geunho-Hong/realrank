package com.project.realrank.product.service;

import com.project.realrank.product.dto.ProductLikeCountReqDto;
import com.project.realrank.product.dto.ProductLikeCountResDto;
import com.project.realrank.product.dto.ProductLikeCountUpdReqDto;
import com.project.realrank.product.repository.ProductMetricsRepository;
import com.project.realrank.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootTest
public class ProductLikeServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMetricsRepository productMetricsRepository;

    @Test
    void 책_좋아요_분산락_미적용_100명_테스트 () throws InterruptedException {
        int numOfThread = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numOfThread);
        CountDownLatch latch = new CountDownLatch(numOfThread);
        final String productCode = "AAAAAAAAAB7QG";
        final String statDate = "20260403";

        ProductLikeCountUpdReqDto productLikeCountUpdReqDto = new ProductLikeCountUpdReqDto(productCode, statDate);

        for (int i = 0; i < numOfThread; i++) {
            executorService.submit(() -> {
                try {
                    productService.increaseLikeCount(productLikeCountUpdReqDto);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        ProductLikeCountResDto productLikeCountResDto = productService.getLikeCount(new ProductLikeCountReqDto(productCode, statDate));
        log.info("Product Code = {} ", productLikeCountResDto.productCode());
        log.info("Product Like Count = {} ", productLikeCountResDto.likeCount());

    }

    @Test
    void 좋아요_Count조회() {
        final String productCode = "AAAAAAAAAB7QG";
        final String statDate = "20260402";
        ProductLikeCountResDto productLikeCountResDto = productService.getLikeCount(new ProductLikeCountReqDto(
                productCode, statDate
        ));
        log.info("product code  = {} ", productLikeCountResDto.productCode());
        log.info("product like count = {} ", productLikeCountResDto.likeCount());
    }

}
