# Phase 7 — 데이터 영속성

## 목표

`InMemory` 구현체를 세 가지 영속성 구현체(FILE / JSON / DATABASE)로 교체해  
애플리케이션 재시작 후에도 데이터가 유지되도록 한다.

- **FILE**: Java 직렬화 (`data/*.dat`)
- **JSON**: Gson 직렬화 (`data/*.json`)
- **DATABASE**: H2 JDBC (`data/ssemi.mv.db`)
- **DummyDataGenerator**: `--tables=all --count=N` CLI 도구

## 사전 확인 (이미 구현됨)

| 항목 | 파일 | 비고 |
|------|------|------|
| `SampleRepository` 인터페이스 | `repository/SampleRepository.java` | save/findById/findAll/update/deleteById |
| `OrderRepository` 인터페이스 | `repository/OrderRepository.java` | save/findById/findAll/findByStatus/update |
| `InMemorySampleRepository` | `repository/impl/InMemorySampleRepository.java` | 교체 대상 |
| `InMemoryOrderRepository` | `repository/impl/InMemoryOrderRepository.java` | 교체 대상 |
| H2, Gson 의존성 | `build.gradle` | 이미 추가됨 |

## 모델 변경

`Sample`, `Order` 두 클래스에 `implements Serializable` 추가  
(FILE 구현체용. JSON·DATABASE 구현체에는 영향 없음)

```java
public class Sample implements Serializable { ... }
public class Order   implements Serializable { ... }
```

## 변경 파일 목록

```
src/main/java/org/example/
├── model/
│   ├── Sample.java                              ← 수정: implements Serializable
│   └── Order.java                               ← 수정: implements Serializable
├── persistence/
│   ├── PersistenceType.java                     ← 신규: FILE / JSON / DATABASE / MEMORY
│   ├── RepositoryFactory.java                   ← 신규
│   └── DatabaseConfig.java                      ← 신규: H2 스키마 초기화
├── repository/
│   └── impl/
│       ├── FileSampleRepository.java            ← 신규
│       ├── FileOrderRepository.java             ← 신규
│       ├── JsonSampleRepository.java            ← 신규
│       ├── JsonOrderRepository.java             ← 신규
│       ├── DatabaseSampleRepository.java        ← 신규
│       └── DatabaseOrderRepository.java         ← 신규
└── generator/
    ├── DataGenerator.java                       ← 신규: 인터페이스
    ├── GeneratorRegistry.java                   ← 신규
    ├── SampleDataGenerator.java                 ← 신규
    ├── OrderDataGenerator.java                  ← 신규
    └── DummyDataGenerator.java                  ← 신규: main 진입점

Application.java                                 ← 수정: RepositoryFactory 적용,
                                                         loadDummySamples() 제거

src/test/java/org/example/
├── repository/impl/
│   ├── FileSampleRepositoryTest.java            ← 신규
│   ├── FileOrderRepositoryTest.java             ← 신규
│   ├── JsonSampleRepositoryTest.java            ← 신규
│   ├── JsonOrderRepositoryTest.java             ← 신규
│   ├── DatabaseSampleRepositoryTest.java        ← 신규
│   └── DatabaseOrderRepositoryTest.java         ← 신규
└── generator/
    └── DummyDataGeneratorTest.java              ← 신규
```

## 클래스 설계

### PersistenceType.java

```java
public enum PersistenceType {
    MEMORY, FILE, JSON, DATABASE;

    public static PersistenceType fromString(String s) {
        return valueOf(s.toUpperCase());
    }
}
```

### RepositoryFactory.java

```java
public class RepositoryFactory {
    private final PersistenceType type;
    private final Path dataDir;        // FILE/JSON: 저장 디렉터리
    private Connection connection;     // DATABASE: 공유 커넥션 (lazy)

    public RepositoryFactory(PersistenceType type) {
        this(type, Path.of("data"));
    }

    // 테스트용 — dataDir 주입
    public RepositoryFactory(PersistenceType type, Path dataDir) { ... }

    public SampleRepository createSampleRepository() {
        return switch (type) {
            case MEMORY   -> new InMemorySampleRepository();
            case FILE     -> new FileSampleRepository(dataDir);
            case JSON     -> new JsonSampleRepository(dataDir);
            case DATABASE -> new DatabaseSampleRepository(getOrInitConnection());
        };
    }

    public OrderRepository createOrderRepository() { ... }

    private Connection getOrInitConnection() {
        if (connection == null) {
            connection = DatabaseConfig.openFile(dataDir);
            DatabaseConfig.initSchema(connection);
        }
        return connection;
    }
}
```

### DatabaseConfig.java

```java
public class DatabaseConfig {
    // 프로덕션: 파일 기반 H2
    public static Connection openFile(Path dataDir) throws SQLException {
        // jdbc:h2:<dataDir>/ssemi
    }

    // 테스트용: 인메모리 H2
    public static Connection openMemory(String dbName) throws SQLException {
        // jdbc:h2:mem:<dbName>;DB_CLOSE_DELAY=-1
    }

    public static void initSchema(Connection conn) throws SQLException {
        // CREATE TABLE IF NOT EXISTS samples (...)
        // CREATE TABLE IF NOT EXISTS orders  (...)
    }
}
```

**테이블 스키마:**

```sql
CREATE TABLE IF NOT EXISTS samples (
    id                 VARCHAR(50)  PRIMARY KEY,
    name               VARCHAR(200) NOT NULL,
    avg_production_time DOUBLE      NOT NULL,
    yield_rate         DOUBLE       NOT NULL,
    stock              INT          NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    order_id      VARCHAR(50)  PRIMARY KEY,
    sample_id     VARCHAR(50)  NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    quantity      INT          NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    ordered_at    VARCHAR(30)  NOT NULL   -- ISO-8601 문자열
);
```

### FileSampleRepository.java / FileOrderRepository.java

```java
public class FileSampleRepository implements SampleRepository {
    private final Path filePath;   // dataDir/samples.dat

    // save 시 전체 Map<String, Sample>을 직렬화해 파일에 씀
    // findAll / findById / update / deleteById 시 파일에서 역직렬화해 메모리로 로드
}
```

- 읽기/쓰기마다 전체 파일을 재직렬화/역직렬화 (단순 구현)
- `ObjectOutputStream` / `ObjectInputStream` 사용

### JsonSampleRepository.java / JsonOrderRepository.java

```java
public class JsonSampleRepository implements SampleRepository {
    private final Path filePath;   // dataDir/samples.json
    private final Gson gson;       // LocalDateTime TypeAdapter 등록

    // Gson TypeAdapter for LocalDateTime:
    // serialize:   LocalDateTime → ISO-8601 문자열
    // deserialize: ISO-8601 문자열 → LocalDateTime
}
```

### DatabaseSampleRepository.java / DatabaseOrderRepository.java

```java
public class DatabaseSampleRepository implements SampleRepository {
    private final Connection conn;

    // save   → INSERT INTO samples ...
    // findById → SELECT ... WHERE id = ?
    // findAll  → SELECT * FROM samples
    // update   → UPDATE samples SET ... WHERE id = ?
    // deleteById → DELETE FROM samples WHERE id = ?
}
```

### DataGenerator 인터페이스 + 구현체

```java
public interface DataGenerator {
    String tableName();
    void generate(Connection conn, int count);
}

public class SampleDataGenerator implements DataGenerator {
    public String tableName() { return "samples"; }
    // 시료 ID: S-0001 ~ S-{count}
    // 이름, avgProductionTime, yieldRate, stock 랜덤 생성
    public void generate(Connection conn, int count) { ... }
}

public class OrderDataGenerator implements DataGenerator {
    // 주문 ID: ORD-{날짜}-{seq}, 고객명 고정값 목록에서 순환, 상태 RESERVED
    public void generate(Connection conn, int count) { ... }
}
```

### GeneratorRegistry.java

```java
public class GeneratorRegistry {
    private final Map<String, DataGenerator> generators = new LinkedHashMap<>();

    public void register(DataGenerator gen) {
        generators.put(gen.tableName(), gen);
    }

    // tables == "all" → 등록된 모든 generator 실행
    // tables == "samples,orders" → 해당 테이블만 실행
    public void run(String tables, Connection conn, int count) { ... }
}
```

### DummyDataGenerator.java

```java
public class DummyDataGenerator {
    // main("--tables=all", "--count=50")
    //   → openFile → initSchema → GeneratorRegistry.run("all", conn, 50)
    public static void main(String[] args) { ... }

    // 테스트 주입용
    public static void run(String tables, int count, Connection conn) { ... }
}
```

## Application.java 변경 내용

```java
// 변경 전
InMemorySampleRepository sampleRepository = new InMemorySampleRepository();
InMemoryOrderRepository  orderRepository  = new InMemoryOrderRepository();
loadDummySamples(sampleRepository);   // ← 제거

// 변경 후
PersistenceType type = PersistenceType.fromString(
    System.getProperty("persistence", "FILE"));
RepositoryFactory factory = new RepositoryFactory(type);
SampleRepository sampleRepository = factory.createSampleRepository();
OrderRepository  orderRepository  = factory.createOrderRepository();
```

실행 시 `./gradlew run --args='-Dpersistence=JSON'` 또는  
`java -Dpersistence=DATABASE -jar app.jar`

## 비즈니스 규칙

- 기본 영속성 타입: `FILE` (`system property` 미지정 시)
- MEMORY 타입은 기존 InMemory 구현체 그대로 사용 (테스트/개발용)
- FILE/JSON: `data/` 디렉터리 없으면 자동 생성
- DATABASE: `data/ssemi.mv.db` 파일. 스키마는 애플리케이션 시작 시 `CREATE TABLE IF NOT EXISTS`로 멱등 초기화
- `DummyDataGenerator`는 독립 실행 가능한 별도 main — 애플리케이션과 공유하지 않음

## TDD 사이클 계획

### 사이클 1 — PersistenceType + RepositoryFactory: MEMORY 타입

- **테스트 이름**: `createSampleRepository_withMemoryType_returnsInMemoryInstance`
- **@DisplayName**: `MEMORY 타입으로 생성한 SampleRepository는 InMemory 구현체다`
- **Given**: `new RepositoryFactory(PersistenceType.MEMORY)`
- **When**: `factory.createSampleRepository()`
- **Then**: `instanceof InMemorySampleRepository`

---

### 사이클 2 — FileSampleRepository: 재시작 후 데이터 유지

- **테스트 이름**: `fileSampleRepository_survivesRestart`
- **@DisplayName**: `save 후 새 인스턴스로 재조회하면 동일한 시료가 반환된다`
- **Given**: 임시 디렉터리, `FileSampleRepository`에 시료 1개 저장
- **When**: 같은 경로로 새 `FileSampleRepository` 인스턴스 생성 → `findAll()`
- **Then**: 저장한 시료의 id/name/stock 일치

---

### 사이클 3 — FileSampleRepository: findById / update / deleteById

- **테스트 이름**: `fileSampleRepository_crudOperations`
- **@DisplayName**: `findById / update / deleteById 가 파일에 올바르게 반영된다`
- **Given**: 시료 저장
- **When**: `findById` → 존재 확인; `update`(stock 변경) → 재조회; `deleteById` → `findById` empty
- **Then**: 각 단계 결과 일치

---

### 사이클 4 — FileOrderRepository: 재시작 후 데이터 유지 + findByStatus

- **테스트 이름**: `fileOrderRepository_survivesRestartAndFiltersByStatus`
- **@DisplayName**: `save 후 새 인스턴스로 재조회 시 데이터 유지 및 findByStatus 동작`
- **Given**: RESERVED 주문 2건, CONFIRMED 주문 1건 저장
- **When**: 새 인스턴스 → `findByStatus(RESERVED)`
- **Then**: 2건 반환, CONFIRMED 제외

---

### 사이클 5 — FileOrderRepository: update 후 재시작 시 상태 유지

- **테스트 이름**: `fileOrderRepository_updatePersistsAcrossRestart`
- **@DisplayName**: `status 변경 후 새 인스턴스로 재조회하면 변경된 상태가 유지된다`
- **Given**: RESERVED 주문 저장 → status를 CONFIRMED로 update
- **When**: 새 인스턴스 → `findById`
- **Then**: status == CONFIRMED

---

### 사이클 6 — JsonSampleRepository: JSON 직렬화 + 재시작 후 유지

- **테스트 이름**: `jsonSampleRepository_survivesRestartWithLocalDateTime`
- **@DisplayName**: `LocalDateTime 포함 시료를 JSON 직렬화·역직렬화해도 값이 보존된다`
- **Given**: 임시 디렉터리, `JsonSampleRepository`에 시료 저장
- **When**: 새 인스턴스 → `findAll()`
- **Then**: id/name/avgProductionTime/yieldRate/stock 일치

---

### 사이클 7 — JsonOrderRepository: findByStatus + update 후 재시작 유지

- **테스트 이름**: `jsonOrderRepository_statusAndUpdatePersist`
- **@DisplayName**: `orderedAt 포함 주문의 findByStatus와 update가 재시작 후에도 유지된다`
- **Given**: RESERVED 주문 저장 → update(CONFIRMED) → 새 인스턴스
- **When**: `findByStatus(CONFIRMED)`
- **Then**: 1건 반환, orderedAt 일치

---

### 사이클 8 — DatabaseConfig: H2 스키마 초기화

- **테스트 이름**: `databaseConfig_initSchema_createsBothTables`
- **@DisplayName**: `initSchema 실행 후 samples·orders 테이블이 존재한다`
- **Given**: `DatabaseConfig.openMemory("schema_test")`
- **When**: `DatabaseConfig.initSchema(conn)`
- **Then**: `INFORMATION_SCHEMA.TABLES` 조회 시 "SAMPLES", "ORDERS" 존재

---

### 사이클 9 — DatabaseSampleRepository: H2 CRUD

- **테스트 이름**: `databaseSampleRepository_fullCrud`
- **@DisplayName**: `H2 인메모리 DB에서 save/findById/update/deleteById가 올바르게 동작한다`
- **Given**: `openMemory` + `initSchema` → `DatabaseSampleRepository`
- **When**: save → findById → update(stock) → findById → deleteById → findById
- **Then**: 각 단계 값 일치, deleteById 후 empty

---

### 사이클 10 — DatabaseOrderRepository: findByStatus + update

- **테스트 이름**: `databaseOrderRepository_findByStatusAndUpdate`
- **@DisplayName**: `findByStatus가 상태별로 주문을 올바르게 필터링하고, update 결과가 DB에 반영된다`
- **Given**: RESERVED 2건, CONFIRMED 1건 저장
- **When**: `findByStatus(RESERVED)` 확인; 1건을 CONFIRMED로 update 후 `findByStatus(CONFIRMED)`
- **Then**: 각 상태별 건수 일치

---

### 사이클 11 — DummyDataGenerator: --count=N 삽입

- **테스트 이름**: `dummyDataGenerator_insertsExpectedCountPerTable`
- **@DisplayName**: `--tables=all --count=5 실행 후 samples·orders 각 5건이 삽입된다`
- **Given**: `openMemory` + `initSchema`; `DummyDataGenerator.run("all", 5, conn)`
- **When**: `SELECT COUNT(*) FROM samples`, `SELECT COUNT(*) FROM orders`
- **Then**: 각각 5

---

### 사이클 12 — RepositoryFactory: FILE/JSON/DATABASE 타입 지원

- **테스트 이름**: `repositoryFactory_createsCorrectImplementations`
- **@DisplayName**: `FILE/JSON/DATABASE 각 타입에 맞는 구현체가 반환된다`
- **Given**: 임시 디렉터리, 각 PersistenceType으로 RepositoryFactory 생성
- **When**: `createSampleRepository()` 호출
- **Then**:
  - FILE → `FileSampleRepository`
  - JSON → `JsonSampleRepository`
  - DATABASE → `DatabaseSampleRepository`
