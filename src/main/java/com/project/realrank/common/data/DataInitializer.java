package com.project.realrank.common.data;

import com.project.realrank.product.domain.ProductCategory;
import com.project.realrank.product.domain.ProductStatus;
import com.project.realrank.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final ProductRepository productRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    // ── 코드 생성 상수 ──────────────────────────────────────────────
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 13;
    private static final int BASE = CHARACTERS.length(); // 36

    // ── 초기화 설정 상수 ─────────────────────────────────────────────
    private static final long   TOTAL_COUNT  = 10_000_000L;
    private static final int    BATCH_SIZE   = 10_000;   // 트랜잭션 commit 단위
    private static final int    THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final long   LOG_INTERVAL = 500_000L; // 50만 건마다 진행률 출력

    // ── 더미 데이터 소스 ─────────────────────────────────────────────
    private static final String[] BOOK_NAMES = {
            "자바의 정석", "클린 코드", "리팩터링", "스프링 부트 핵심 가이드",
            "JPA 프로그래밍", "HTTP 완벽 가이드", "운영체제 공룡책", "알고리즘 문제 풀이",
            "데이터베이스 첫걸음", "컴퓨터 구조와 운영체제", "파이썬 완전 정복",
            "모던 자바스크립트", "클라우드 네이티브", "마이크로서비스 패턴"
    };
    private static final String[] STATIONARY_NAMES = {
            "모나미 볼펜", "파일럿 만년필", "스테들러 연필", "포스트잇 노트",
            "무인양품 노트", "캠퍼스 줄 노트", "화이트보드 마커", "형광펜 세트",
            "지우개 모음", "A4 용지 500매", "클립 보드", "인덱스 탭 세트"
    };
    private static final String[] DESCRIPTIONS = {
            "베스트셀러 상품입니다.", "입문자에게 추천합니다.", "전문가를 위한 심화 내용입니다.",
            "개정판 최신 내용 반영.", "실무 예제 중심으로 구성.", "전국 서점 1위 상품.",
            "강의와 병행하기 좋은 교재.", "한정 수량 특가 상품."
    };

    // ─────────────────────────────────────────────────────────────────
    //  외부 진입점 (Controller에서 호출)
    //  - 이미 실행 중이면 즉시 반환
    //  - 이미 데이터가 있으면 즉시 반환
    // ─────────────────────────────────────────────────────────────────

    public void initialize() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("[DataInitializer] 이미 실행 중입니다.");
            return;
        }

        if (productRepository.count() > 0) {
            log.info("[DataInitializer] 이미 데이터가 존재합니다. 초기화를 건너뜁니다.");
            isRunning.set(false);
            return;
        }

        log.info("[DataInitializer] 더미 데이터 {}건 생성 시작 (스레드: {}, 배치: {}건/commit)",
                TOTAL_COUNT, THREAD_COUNT, BATCH_SIZE);

        long startTime = System.currentTimeMillis();
        try {
            insertDummyProducts();
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[DataInitializer] 완료. 총 소요시간: {}ms ({}초)", elapsed, elapsed / 1000);
        } finally {
            isRunning.set(false);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  멀티스레드 배치 삽입
    //  - 전체 배치를 THREAD_COUNT 개의 스레드에 분산
    //  - 각 배치(BATCH_SIZE 건)가 독립적인 트랜잭션으로 commit
    // ─────────────────────────────────────────────────────────────────

    private void insertDummyProducts() {
        long totalBatches = TOTAL_COUNT / BATCH_SIZE;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicLong completedCount = new AtomicLong(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>((int) totalBatches);

        for (long batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
            final long startIndex = batchIndex * BATCH_SIZE;

            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    () -> insertBatch(startIndex, completedCount),
                    executor
            );
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> {
                    log.error("[DataInitializer] 배치 처리 중 오류 발생", ex);
                    return null;
                })
                .join();

        executor.shutdown();
    }

    // ─────────────────────────────────────────────────────────────────
    //  단일 배치 삽입 (BATCH_SIZE 건 = 트랜잭션 1건)
    //  TransactionTemplate: @Transactional은 스레드 경계를 넘지 못하므로
    //  프로그래밍 방식의 트랜잭션 사용
    // ─────────────────────────────────────────────────────────────────

    private void insertBatch(long startIndex, AtomicLong completedCount) {
        transactionTemplate.execute(status -> {
            jdbcTemplate.batchUpdate(
                    "INSERT INTO tbl_product (product_code, name, price, category, status, description) "
                            + "VALUES (?, ?, ?, ?, ?, ?)",
                    new BatchPreparedStatementSetter() {

                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            long globalIndex = startIndex + i;
                            ProductCategory category = resolveCategory(globalIndex);

                            ps.setString(1, generateProductCodeByIndex(globalIndex));
                            ps.setString(2, generateName(category, globalIndex));
                            ps.setBigDecimal(3, generatePrice(globalIndex));
                            ps.setString(4, category.getCode());
                            ps.setString(5, ProductStatus.ACTIVE.name());
                            ps.setString(6, generateDescription(globalIndex));
                        }

                        @Override
                        public int getBatchSize() {
                            return BATCH_SIZE;
                        }
                    }
            );
            return null;
        });

        long count = completedCount.addAndGet(BATCH_SIZE);
        if (count % LOG_INTERVAL == 0) {
            log.info("[DataInitializer] 진행률: {}/{} ({}%)",
                    count, TOTAL_COUNT, count * 100 / TOTAL_COUNT);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  더미 데이터 생성 헬퍼
    // ─────────────────────────────────────────────────────────────────

    private ProductCategory resolveCategory(long index) {
        return (index % 2 == 0) ? ProductCategory.BOOK : ProductCategory.STATIONARY;
    }

    private String generateName(ProductCategory category, long index) {
        if (category == ProductCategory.BOOK) {
            String base = BOOK_NAMES[(int) (index % BOOK_NAMES.length)];
            long edition = index / BOOK_NAMES.length + 1;
            return base + " " + edition + "판";
        }
        String base = STATIONARY_NAMES[(int) (index % STATIONARY_NAMES.length)];
        long vol = index / STATIONARY_NAMES.length + 1;
        return base + " vol." + vol;
    }

    private BigDecimal generatePrice(long index) {
        // 1,000 ~ 99,000 원 (1,000원 단위)
        long price = 1_000L + (index % 99) * 1_000L;
        return BigDecimal.valueOf(price);
    }

    private String generateDescription(long index) {
        return DESCRIPTIONS[(int) (index % DESCRIPTIONS.length)];
    }

    // ─────────────────────────────────────────────────────────────────
    //  13자리 고유 상품코드 생성 유틸리티
    // ─────────────────────────────────────────────────────────────────

    /**
     * 인덱스 기반 13자리 상품코드 생성 (대량 데이터 전용)
     * index가 동일하면 항상 같은 코드 → HashSet 없이 uniqueness 보장
     */
    public static String generateProductCodeByIndex(long index) {
        char[] result = new char[CODE_LENGTH];
        for (int i = CODE_LENGTH - 1; i >= 0; i--) {
            result[i] = CHARACTERS.charAt((int) (index % BASE));
            index /= BASE;
        }
        return new String(result);
    }
}
