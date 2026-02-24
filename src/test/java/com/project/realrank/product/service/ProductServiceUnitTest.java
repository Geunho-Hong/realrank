package com.project.realrank.product.service;

import com.project.realrank.product.domain.Product;
import com.project.realrank.product.domain.ProductCategory;
import com.project.realrank.product.domain.ProductStatus;
import com.project.realrank.product.dto.ProductCreateReqDto;
import com.project.realrank.product.dto.ProductCreateResDto;
import com.project.realrank.product.dto.ProductSearchResDto;
import com.project.realrank.product.dto.ProductUpdReqDto;
import com.project.realrank.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("상품_생성이_정상적으로_처리되는지_확인")
    void 상품_생성이_정상적으로_처리되는지_확인() {

        // given
        ProductCreateReqDto reqDto = new ProductCreateReqDto(
                "P-001",
                "테스트상품",
                BigDecimal.valueOf(12000),
                "BOOK",
                "설명"
        );

        given(productRepository.save(any(Product.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        ProductCreateResDto resDto = productService.createProduct(reqDto);

        // then
        then(productRepository).should().save(any(Product.class));
        assertThat(resDto.productCode()).isEqualTo("P-001");
        assertThat(resDto.category()).isEqualTo(ProductCategory.BOOK);
    }

    @Test
    @DisplayName("상품코드로_상품조회가_정상적으로_처리되는지_확인")
    void 상품코드로_상품조회가_정상적으로_처리되는지_확인() {

        // given
        Product product = new Product(
                "P-001",
                "테스트상품",
                BigDecimal.valueOf(12000),
                ProductCategory.BOOK,
                ProductStatus.ACTIVE,
                "설명"
        );

        given(productRepository.getProductByProductCode("P-001"))
                .willReturn(Optional.of(product));

        // when
        ProductSearchResDto resDto = productService.getProduct("P-001");

        // then
        assertThat(resDto.productCode()).isEqualTo("P-001");
        assertThat(resDto.category()).isEqualTo(ProductCategory.BOOK);
    }

    @Test
    @DisplayName("상품코드가_없으면_예외가_발생하는지_확인")
    void 상품코드가_없으면_예외가_발생하는지_확인() {

        // given
        given(productRepository.getProductByProductCode("P-404"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getProduct("P-404"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("상품명으로_상품목록_조회가_정상적으로_처리되는지_확인")
    void 상품명으로_상품목록_조회가_정상적으로_처리되는지_확인() {

        // given
        Product product = new Product(
                "P-001",
                "테스트상품",
                BigDecimal.valueOf(12000),
                ProductCategory.BOOK,
                ProductStatus.ACTIVE,
                "설명"
        );

        given(productRepository.getProductsByNameLike("테스%"))
                .willReturn(List.of(product));

        // when
        List<ProductSearchResDto> result = productService.getProductsByName("테스");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).productCode()).isEqualTo("P-001");
    }

    @Test
    @DisplayName("상품_수정이_정상적으로_처리되는지_확인")
    void 상품_수정이_정상적으로_처리되는지_확인() {

        // given
        Product product = new Product(
                "P-001",
                "테스트상품",
                BigDecimal.valueOf(12000),
                ProductCategory.BOOK,
                ProductStatus.ACTIVE,
                "설명"
        );

        ProductUpdReqDto reqDto = new ProductUpdReqDto(
                "P-001",
                "수정상품",
                BigDecimal.valueOf(15000),
                "STATIONARY",
                "수정설명"
        );

        given(productRepository.getProductByProductCode("P-001"))
                .willReturn(Optional.of(product));

        // when
        boolean result = productService.updateProduct(reqDto);

        // then
        assertThat(result).isTrue();
        assertThat(product.getName()).isEqualTo("수정상품");
        assertThat(product.getCategory()).isEqualTo(ProductCategory.STATIONARY);
        assertThat(product.getPrice()).isEqualTo(BigDecimal.valueOf(15000));
    }

    @Test
    @DisplayName("상품_삭제가_정상적으로_처리되는지_확인")
    void 상품_삭제가_정상적으로_처리되는지_확인() {

        // given
        Product product = new Product(
                "P-001",
                "테스트상품",
                BigDecimal.valueOf(12000),
                ProductCategory.BOOK,
                ProductStatus.ACTIVE,
                "설명"
        );

        given(productRepository.getProductByProductCode("P-001"))
                .willReturn(Optional.of(product));

        // when
        boolean result = productService.deleteProduct("P-001");

        // then
        assertThat(result).isTrue();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    }
}
