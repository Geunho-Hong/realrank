# DataInitializer 구현 설명

> 파일 위치: `src/main/java/com/project/realrank/common/data/DataInitializer.java`

---

## 1. 전체 구조 한눈에 보기

```
POST /api/admin/data/init (DataInitializerController)
        │
        └─► DataInitializer.initialize()          ← 진입점, 중복/기존 데이터 방어
                │
                └─► insertDummyProducts()          ← 멀티스레드 배치 분배
                        │
                        ├─► insertBatch(0 ~ 9999)     ← 스레드 1 (트랜잭션 1건)
                        ├─► insertBatch(10000 ~ 19999) ← 스레드 2 (트랜잭션 1건)
                        ├─► ...
                        └─► insertBatch(9990000 ~ 9999999) ← 트랜잭션 1,000번째
```

- 총 1,000만 건을 **10,000건짜리 배치 1,000개**로 나눔
- 각 배치는 **독립적인 트랜잭션(commit 1회)**으로 처리
- HTTP 요청은 즉시 202 반환, 실제 insert는 **백그라운드에서 비동기** 실행

---

## 2. 핵심 설계 포인트

### 2-1. JPA saveAll() 대신 JdbcTemplate.batchUpdate()를 쓴 이유

```java
jdbcTemplate.batchUpdate("INSERT INTO tbl_product ...", new BatchPreparedStatementSetter() { ... });
```

JPA에서 PK 생성 전략으로 `GenerationType.IDENTITY`를 사용하면 **Hibernate의 배치 INSERT가 자동으로 비활성화**된다.

| 방법 | 동작 | 1,000만 건 성능 |
|---|---|---|
| `JpaRepository.saveAll()` | IDENTITY 전략 → INSERT 1건마다 DB 왕복 1회 | 매우 느림 |
| `JdbcTemplate.batchUpdate()` | JDBC 드라이버가 여러 INSERT를 묶어서 전송 | 빠름 |

MySQL JDBC URL에 `rewriteBatchedStatements=true` 옵션을 추가해야 실제로 패킷을 묶어서 전송한다.
(application.yml에 이미 설정 완료)

---

### 2-2. @Transactional 대신 TransactionTemplate을 쓴 이유

```java
transactionTemplate.execute(status -> {
    jdbcTemplate.batchUpdate(...);
    return null;
});
```

`@Transactional`은 **Spring AOP 프록시** 방식으로 동작한다.
즉, 메서드를 호출한 스레드의 트랜잭션 컨텍스트에 묶인다.

멀티스레드 환경에서는 `ExecutorService`가 만든 **별도 스레드**에서 insert가 실행되는데,
이 스레드들은 원래 호출 스레드의 트랜잭션을 **공유하지 않는다.**

따라서 `@Transactional`을 붙여도 각 스레드는 트랜잭션 없이 동작하게 되므로,
`TransactionTemplate`으로 **각 배치 내부에서 직접 트랜잭션을 열고 닫는** 방식을 사용했다.

```
스레드 A: TransactionTemplate.execute() → BEGIN → 10,000건 INSERT → COMMIT
스레드 B: TransactionTemplate.execute() → BEGIN → 10,000건 INSERT → COMMIT
스레드 C: TransactionTemplate.execute() → BEGIN → 10,000건 INSERT → COMMIT
          (셋이 동시에 실행, 각자 독립적인 트랜잭션)
```

---

### 2-3. 트랜잭션 commit 단위를 10,000건으로 설정한 이유

```java
private static final int BATCH_SIZE = 10_000;
```

commit 단위는 **너무 작아도, 너무 커도** 문제가 된다.

| 단위 | 문제 |
|---|---|
| 1건 per commit | commit 오버헤드 1,000만 회 → 극도로 느림 |
| 1,000만 건 전체를 1번 commit | 실패 시 전체 롤백, 트랜잭션 유지 중 메모리·Lock 점유 과다 |
| **10,000건 per commit** | commit 횟수 1,000회, 실패 시 최대 10,000건만 롤백 |

---

### 2-4. AtomicBoolean으로 중복 실행 방지

```java
private final AtomicBoolean isRunning = new AtomicBoolean(false);

if (!isRunning.compareAndSet(false, true)) {
    log.warn("[DataInitializer] 이미 실행 중입니다.");
    return;
}
```

`compareAndSet(false, true)`는 **읽기 + 쓰기를 원자적(atomic)으로** 수행한다.
일반 `if (isRunning)` 체크는 두 스레드가 동시에 `false`를 읽고 둘 다 실행을 시작할 수 있는 **Race Condition**이 발생한다.
`compareAndSet`은 이를 하드웨어 수준에서 막아준다.

```
스레드 A: isRunning이 false인가? → true이면 false→true로 변경하고 실행 (성공)
스레드 B: isRunning이 false인가? → 이미 true이므로 변경 실패 → 즉시 반환 (차단)
```

`finally` 블록에서 `isRunning.set(false)`로 초기화하여, 완료 후 다시 실행 가능한 상태로 복구한다.

---

### 2-5. CompletableFuture로 비동기 병렬 처리

```java
// 컨트롤러 - HTTP 요청 스레드를 블로킹하지 않고 즉시 반환
CompletableFuture.runAsync(dataInitializer::initialize);

// DataInitializer 내부 - 배치를 병렬로 실행하고 전부 완료될 때까지 대기
CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
```

| 위치 | 역할 |
|---|---|
| 컨트롤러 | HTTP 응답(202)은 즉시 반환, 초기화는 백그라운드에서 실행 |
| insertDummyProducts | 1,000개 배치를 스레드풀에 던져 병렬 실행, `.join()`으로 전부 완료 대기 |

---

### 2-6. 13자리 고유 상품코드 생성 (36진수 인코딩)

```java
private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // 36가지 문자
private static final int CODE_LENGTH = 13;

public static String generateProductCodeByIndex(long index) {
    char[] result = new char[CODE_LENGTH];
    for (int i = CODE_LENGTH - 1; i >= 0; i--) {
        result[i] = CHARACTERS.charAt((int) (index % BASE)); // 나머지 → 문자
        index /= BASE;                                        // 몫으로 다음 자리 계산
    }
    return new String(result);
}
```

10진수를 36진수로 변환하는 방식이다. 10진수 숫자를 36으로 나눈 **나머지**가 각 자리의 문자를 결정한다.

```
index = 0       → "AAAAAAAAAAAAA"
index = 1       → "AAAAAAAAAAAAB"
index = 35      → "AAAAAAAAAAAAZ"
index = 36      → "AAAAAAAAAAAB0"
index = 10000000 → "AAAAAAAADIZS"
```

**왜 HashSet 없이도 중복이 없는가?**
같은 index에서 항상 같은 문자열이 나오고, 서로 다른 index는 반드시 서로 다른 문자열이 나오기 때문이다.
(일대일 함수이므로 중복 불가)

36^13 ≈ 3.2 × 10^20 가지 조합이 가능하므로 1,000만 건은 아주 작은 범위에 불과하다.

---

## 3. 전체 플로우 시각화

```
POST /api/admin/data/init
        │
        │  202 Accepted 즉시 반환
        │
CompletableFuture.runAsync()
        │
DataInitializer.initialize()
        ├─ [중복 실행?] isRunning.compareAndSet(false, true) 실패 → return
        ├─ [데이터 있음?] productRepository.count() > 0 → return
        │
        └─ insertDummyProducts()
                │
                ExecutorService (고정 스레드풀: CPU 코어 수)
                │
                ├─ CompletableFuture batch #0   (index 0 ~ 9999)
                │       TransactionTemplate → BEGIN
                │       JdbcTemplate.batchUpdate() → 10,000건 INSERT (패킷 묶음 전송)
                │       COMMIT
                │
                ├─ CompletableFuture batch #1   (index 10000 ~ 19999)
                │       ...동일...
                │
                ├─ ... (총 1,000개 배치가 병렬 실행)
                │
                └─ CompletableFuture.allOf(...).join()  ← 전부 완료까지 대기
                        │
                        완료 로그 출력 → isRunning.set(false)
```

---

## 4. 관련 설정 (application.yml)

```yaml
datasource:
  url: jdbc:mysql://...?rewriteBatchedStatements=true  # JDBC 레벨 배치 패킷 묶음 (필수)
  hikari:
    maximum-pool-size: 20   # 스레드 수보다 여유 있게 설정
    minimum-idle: 10
```

`rewriteBatchedStatements=true` 없이는 `batchUpdate()`를 호출해도 MySQL 드라이버가 INSERT를 개별 전송한다.
이 옵션이 있어야 여러 INSERT를 하나의 패킷으로 묶어 네트워크 왕복 횟수를 줄인다.
