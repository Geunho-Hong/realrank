package com.project.realrank.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.realrank.product.domain.Product;
import com.project.realrank.product.domain.ProductCategory;
import com.project.realrank.product.domain.ProductStatus;
import com.project.realrank.product.dto.ProductCreateReqDto;
import com.project.realrank.product.dto.ProductUpdReqDto;
import com.project.realrank.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("상품_생성_후_단건조회가_정상적으로_처리되는지_확인")
    void 상품_생성_후_단건조회가_정상적으로_처리되는지_확인() throws Exception {

        // given
        ProductCreateReqDto createReqDto = new ProductCreateReqDto(
                "P-001",
                "테스트상품",
                BigDecimal.valueOf(12000),
                "BOOK",
                "설명"
        );

        // when & then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.productCode").value("P-001"));

        mockMvc.perform(get("/api/products/P-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.name").value("테스트상품"))
                .andExpect(jsonPath("$.data.category").value("BOOK"));
    }

    @Test
    @DisplayName("상품_수정이_정상적으로_처리되는지_확인")
    void 상품_수정이_정상적으로_처리되는지_확인() throws Exception {

        // given
        Product product = new Product(
                "P-002",
                "원본상품",
                BigDecimal.valueOf(9000),
                ProductCategory.BOOK,
                ProductStatus.ACTIVE,
                "원본문구"
        );
        productRepository.save(product);

        ProductUpdReqDto updReqDto = new ProductUpdReqDto(
                "P-002",
                "수정상품",
                BigDecimal.valueOf(15000),
                "STATIONARY",
                "수정설명"
        );

        // when & then
        mockMvc.perform(patch("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updReqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(get("/api/products/P-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정상품"))
                .andExpect(jsonPath("$.data.category").value("STATIONARY"))
                .andExpect(jsonPath("$.data.description").value("수정설명"));
    }

    @Test
    @DisplayName("상품_삭제가_정상적으로_처리되는지_확인")
    void 상품_삭제가_정상적으로_처리되는지_확인() throws Exception {

        // given
        Product product = new Product(
                "P-003",
                "삭제상품",
                BigDecimal.valueOf(5000),
                ProductCategory.BOOK,
                ProductStatus.ACTIVE,
                "삭제설명"
        );
        productRepository.save(product);

        // when & then
        mockMvc.perform(delete("/api/products/P-003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data").value(true));

        Product updated = productRepository.getProductByProductCode("P-003").orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    }
}
