package com.project.realrank.product.controller;

import com.project.realrank.common.constant.ApiResponse;
import com.project.realrank.product.dto.ProductCreateReqDto;
import com.project.realrank.product.dto.ProductCreateResDto;
import com.project.realrank.product.dto.ProductUpdReqDto;
import com.project.realrank.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createProduct(@Valid @RequestBody ProductCreateReqDto productCreateReqDto) {
        return ResponseEntity.ok(ApiResponse.created(productService.createProduct(productCreateReqDto)));
    }

    @GetMapping("/{productCode}")
    public ResponseEntity<ApiResponse<?>> getProduct(@PathVariable String productCode) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProduct(productCode)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getProducts(@RequestParam(name = "name") String name) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductsByName(name)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getAllProducts(page,size)));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<?>> updateProduct(@Valid @RequestBody ProductUpdReqDto productUpdReqDto) {
        return ResponseEntity.ok(ApiResponse.ok(productService.updateProduct(productUpdReqDto)));
    }

    @DeleteMapping("/{productCode}")
    public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable String productCode) {
        return ResponseEntity.ok(ApiResponse.ok(productService.deleteProduct(productCode)));
    }


}
