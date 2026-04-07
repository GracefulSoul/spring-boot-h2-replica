# Spring Boot H2 Replica Configuration

Spring Boot 애플리케이션에서 **Primary-Replica DataSource 구성**과 **Transaction Routing**을 구현한 프로젝트입니다. H2 Database를 사용하여 읽기(Read)와 쓰기(Write) 작업을 분리하며, AOP와 Annotation을 통해 자동으로 적절한 DataSource로 라우팅합니다.

## 주요 기능

- ✅ **Primary-Replica DataSource 구성**: H2 메모리 데이터베이스 2개로 구성
- ✅ **자동 DataSource 라우팅**: Abstract RoutingDataSource를 사용한 자동 라우팅
- ✅ **AOP 기반의 라우팅 제어**: @ReadOnlyOnReplica 및 @Transactional(readOnly) 지원
- ✅ **ThreadLocal 기반 컨텍스트**: 멀티스레드 환경에서 안전한 DataSource 관리
- ✅ **포괄적인 테스트**: 통합 테스트 및 단위 테스트 포함
- ✅ **상세한 로깅**: 현재 사용 중인 DataSource를 로그에 기록

## 프로젝트 구조

```
spring-boot-h2-replica/
├── src/
│   ├── main/
│   │   ├── java/com/gracefulsoul/replica/
│   │   │   ├── SpringBootH2ReplicaApplication.java    # 메인 애플리케이션
│   │   │   ├── aspect/
│   │   │   │   └── DataSourceRoutingAspect.java       # AOP Aspect for 라우팅
│   │   │   ├── config/
│   │   │   │   └── DataSourceConfig.java              # DataSource 설정
│   │   │   ├── controller/
│   │   │   │   └── UserController.java                # REST API Controller
│   │   │   ├── entity/
│   │   │   │   ├── User.java                          # 사용자 Entity
│   │   │   │   └── UserLog.java                       # 사용자 로그 Entity
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java                # User Repository
│   │   │   │   └── UserLogRepository.java             # UserLog Repository
│   │   │   ├── routing/
│   │   │   │   ├── RouteDataSourceContext.java        # DataSource 컨텍스트 (ThreadLocal)
│   │   │   │   ├── ReadWriteRoutingDataSource.java    # AbstractRoutingDataSource 구현
│   │   │   │   └── ReadOnlyOnReplica.java             # 커스텀 Annotation
│   │   │   └── service/
│   │   │       ├── UserService.java                   # User 서비스
│   │   │       └── UserLogService.java                # UserLog 서비스
│   │   └── resources/
│   │       └── application.yml                        # 애플리케이션 설정
│   └── test/
│       └── java/com/gracefulsoul/replica/
│           ├── DataSourceContextTest.java             # 단위 테스트
│           └── integration/
│               └── DataSourceRoutingIntegrationTest.java  # 통합 테스트
├── pom.xml                                             # Maven 설정
└── README.md                                           # 프로젝트 README
```

## 기술 스택

| 항목 | 버전 |
|------|------|
| **Java** | 25 |
| **Spring Boot** | 3.3.0 |
| **H2 Database** | 2.2.224 |
| **HikariCP** | 5.1.0 |
| **JUnit** | 5 |
| **Lombok** | Latest |

## 빠른 시작

### 1. 프로젝트 클론 및 빌드

```bash
# 프로젝트 디렉토리로 이동
cd spring-boot-h2-replica

# Maven을 사용하여 빌드
mvn clean build

# 애플리케이션 시작
mvn spring-boot:run
```

### 2. API 사용 예제

#### 사용자 생성 (Primary DataSource 사용)
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "GracefulSoul",
    "email": "gracefulsoul@github.com",
    "phone": "010-1234-5678",
    "address": "서울시 강남구",
    "active": true
  }'
```

#### 모든 사용자 조회 (Replica DataSource 사용)
```bash
curl -X GET http://localhost:8080/api/users
```

#### ID로 사용자 조회 (Replica DataSource 사용)
```bash
curl -X GET http://localhost:8080/api/users/1
```

#### 사용자 수정 (Primary DataSource 사용)
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "GracefulSoul_수정",
    "email": "gracefulsoul_updated@github.com",
    "phone": "010-9876-5432",
    "active": true
  }'
```

#### 사용자 삭제 (Primary DataSource 사용)
```bash
curl -X DELETE http://localhost:8080/api/users/1
```

## 핵심 개념

### DataSource Routing 메커니즘

```
요청
  ↓
AOP Aspect 가로채기
  ├─ @ReadOnlyOnReplica 확인
  ├─ @Transactional(readOnly=true) 확인
  └─ RouteDataSourceContext 업데이트
  ↓
ReadWriteRoutingDataSource.determineCurrentLookupKey()
  ├─ RouteDataSourceContext.getDataSourceType() 호출
  ├─ PRIMARY 또는 REPLICA 결정
  └─ 해당 DataSource 반환
  ↓
데이터베이스 작업 실행
  ↓
AOP @After advice
  └─ DataSource 컨텍스트 초기화
```

### 주요 클래스 설명

#### 1. `RouteDataSourceContext`
- **역할**: ThreadLocal을 사용하여 현재 스레드의 DataSource 타입을 관리
- **메서드**:
  - `setDataSourceType(DataSourceType)`: DataSource 타입 설정
  - `getDataSourceType()`: 현재 DataSource 타입 조회
  - `setPrimary()`: PRIMARY로 설정
  - `setReplica()`: REPLICA로 설정
  - `clear()`: 기본값(PRIMARY)으로 초기화

#### 2. `ReadWriteRoutingDataSource`
- **역할**: AbstractRoutingDataSource를 상속하여 라우팅 로직 구현
- **메서드**:
  - `determineCurrentLookupKey()`: RouteDataSourceContext 값을 기반으로 DataSource 결정

#### 3. `DataSourceRoutingAspect`
- **역할**: AOP를 사용하여 자동 라우팅 제어
- **기능**:
  - `@ReadOnlyOnReplica` 메서드 호출 시 Replica로 설정
  - `@Transactional(readOnly=true)` 메서드 호출 시 Replica로 설정
  - 메서드 완료 후 Primary로 복원

#### 4. `DataSourceConfig`
- **역할**: Primary 및 Replica DataSource Bean 설정
- **구성**:
  - Primary DataSource: Write 작업
  - Replica DataSource: Read 작업
  - ReadWriteRoutingDataSource: 라우팅 관리

## 라우팅 규칙

| 상황 | 사용 DataSource | 설정 방법 |
|------|-----------------|---------|
| INSERT/UPDATE/DELETE | PRIMARY | 기본값 (명시 필요 X) |
| SELECT | REPLICA | @ReadOnlyOnReplica 또는 @Transactional(readOnly=true) |
| 트랜잭션 없음 | PRIMARY | 기본값 |

## 테스트 실행

### 모든 테스트 실행
```bash
mvn test
```

### 특정 테스트 클래스 실행
```bash
mvn test -Dtest=DataSourceRoutingIntegrationTest
mvn test -Dtest=DataSourceContextTest
```

## H2 콘솔 접근

H2 데이터베이스를 시각적으로 확인하려면 H2 Console을 사용할 수 있습니다.

### application.yml에 다음 추가:
```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

### 접근 방법
1. 애플리케이션을 실행한 후:
2. 브라우저에서 `http://localhost:8080/h2-console` 접근
3. JDBC URL: `jdbc:h2:mem:primary` (또는 `replica`)
4. 사용자명: `sa`
5. 비밀번호: (없음)

## 로깅 설정

로그에서 현재 사용 중인 DataSource를 확인할 수 있습니다:

```
[2024-04-07 10:30:45] [main] DEBUG com.gracefulsoul.replica.aspect.DataSourceRoutingAspect - 메서드 시작: UserService.getAllUsers -> Replica DataSource 선택됨
[2024-04-07 10:30:46] [main] DEBUG com.gracefulsoul.replica.aspect.DataSourceRoutingAspect - 메서드 완료: UserService.getAllUsers -> Primary DataSource로 복원됨
```

## 성능 고려사항

1. **Connection Pool**: HikariCP를 사용하여 효율적인 연결 관리
2. **ThreadLocal**: 각 스레드별로 독립적인 컨텍스트 관리로 스레드 안전성 보장
3. **AOP Overhead**: 약간의 성능 오버헤드가 있지만 라우팅의 자동화 이점이 큼

## 주의사항

1. **Replica에서 쓰기 작업**: @ReadOnlyOnReplica 메서드 내에서 INSERT/UPDATE/DELETE를 시도하면 오류 발생
2. **트랜잭션 경계**: 중첩된 트랜잭션에서는 가장 바깥쪽 메서드의 readOnly 설정이 우선됨
3. **컨텍스트 정리**: 멀티스레드 환경에서는 ThreadLocal 때문에 자동으로 정리됨

## 참고 자료

- [Spring Data JPA 문서](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring AOP 문서](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#aop)
- [AbstractRoutingDataSource 공식 문서](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/datasource/lookup/AbstractRoutingDataSource.html)
- [H2 Database 공식 웹사이트](http://www.h2database.com/)

## 라이선스

MIT License

## 기여

버그 리포트 및 개선 사항은 이슈를 통해 제출해주세요.
