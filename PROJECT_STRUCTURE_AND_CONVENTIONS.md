# 프로젝트 구조 및 코드 컨벤션

이 문서는 현재 프로젝트의 디렉터리 구조와 코드 작성 관례를 요약합니다.

**프로젝트 구조**
```text
.
├─ build.gradle
├─ docker-compose.yml
├─ gradle/
├─ gradlew
├─ gradlew.bat
├─ settings.gradle
├─ src/
│  ├─ main/
│  │  ├─ java/com/project/realrank/
│  │  │  ├─ RealtimeRankingServiceApplication.java
│  │  │  ├─ common/
│  │  │  │  ├─ aspect/
│  │  │  │  ├─ constant/
│  │  │  │  └─ exception/
│  │  │  ├─ config/db/
│  │  │  └─ product/
│  │  │     ├─ controller/
│  │  │     ├─ domain/
│  │  │     ├─ dto/
│  │  │     ├─ repository/
│  │  │     └─ service/
│  │  └─ resources/
│  │     └─ application.yml
│  └─ test/java/com/project/realrank/
│     └─ RealtimeRankingServiceApplicationTests.java
└─ Readme.md
```

**패키지 및 책임**
- `com.project.realrank` : Spring Boot 엔트리 포인트.
- `common.aspect` : 공통 관심사(AOP) 처리 (HTTP 요청 로깅).
- `common.constant` : 공통 응답 모델 및 에러 코드.
- `common.exception` : 전역 예외 처리.
- `config.db` : JPA 설정(현재 주석 처리됨).
- `product` : 상품 API 기능 모듈.

**레이어링 및 네이밍**
- 컨트롤러는 `*.controller`에 위치하고 `@RestController` 사용.
- 서비스는 `*.service`에 위치하고 `@Service`, `@RequiredArgsConstructor`, `@Transactional` 사용.
- 리포지토리는 `*.repository`에 위치하며 `JpaRepository`를 상속하고 `getProductByProductCode` 같은 메서드 네이밍 규칙 사용.
- 엔티티는 `*.domain`에 위치하며 `@Entity`와 Lombok 사용.
- DTO는 `*.dto`에 위치하며 Java `record` 타입으로 정의.
- DTO 네이밍은 `*ReqDto`, `*ResDto`, `*SearchResDto` 같은 접미어 사용.

**API 컨벤션**
- 기본 URL 패턴은 `/api/...` (예: `/api/products`).
- 표준 응답 래퍼: `ApiResponse<T>`와 헬퍼 메서드 `ok`, `created`, `error` 사용.
- 검증은 `@Valid`와 Bean Validation 애노테이션으로 처리.

**도메인 및 영속성**
- 엔티티는 `@Table(name = "tbl_...")`로 테이블명을 명시.
- Enum은 `@Enumerated(EnumType.STRING)`으로 문자열 저장.
- 엔티티/DTO 변환은 `from(...)` 같은 static factory 메서드 사용.

**에러 처리**
- `GlobalExceptionHandler`에서 `IllegalArgumentException`, `MethodArgumentNotValidException`, `Exception`을 처리.
- 전역 예외 처리 의도라면 `@RestControllerAdvice` 또는 `@ControllerAdvice` 추가 필요.

**로깅**
- `LoggingAspect`에서 `product` 패키지의 컨트롤러 요청 메타데이터를 로깅.

**빌드 및 런타임**
- Java 17 toolchain 사용.
- Spring Boot 3.4.11, Spring Web, Spring Data JPA, Validation, Lombok 사용.
- MySQL 드라이버는 `application.yml`에 설정.

**테스트**
- `src/test/java`에 기본 Spring context 로드 테스트 존재.
