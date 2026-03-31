package com.project.realrank.common.data;

import com.project.realrank.common.constant.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/admin/data")
@RequiredArgsConstructor
public class DataInitializerController {

    private final DataInitializer dataInitializer;

    /**
     * 더미 데이터 초기화 트리거
     * 백그라운드에서 비동기 실행되며 즉시 202 반환
     * POST /api/admin/data/init
     */
    @PostMapping("/init")
    public ResponseEntity<ApiResponse<?>> init() {
        CompletableFuture.runAsync(dataInitializer::initialize);
        return ResponseEntity.accepted()
                .body(ApiResponse.ok("더미 데이터 생성을 시작했습니다. 서버 로그에서 진행률을 확인하세요."));
    }
}
