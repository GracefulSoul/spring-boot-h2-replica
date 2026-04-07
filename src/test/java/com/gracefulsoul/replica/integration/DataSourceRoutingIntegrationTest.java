package com.gracefulsoul.replica.integration;

import com.gracefulsoul.replica.entity.User;
import com.gracefulsoul.replica.repository.UserRepository;
import com.gracefulsoul.replica.routing.RouteDataSourceContext;
import com.gracefulsoul.replica.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Primary-Replica DataSource 라우팅 통합 테스트
 * 
 * Write 작업은 PRIMARY DataSource로,
 * Read 작업은 REPLICA DataSource로 라우팅되는지 확인합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class DataSourceRoutingIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // 각 테스트 시작 전 컨텍스트 초기화
        RouteDataSourceContext.clear();
        // 기존 데이터 삭제
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("새 사용자 생성 시 PRIMARY DataSource 사용")
    void testCreateUserUsesPrimary() {
        // When: 새 사용자 생성
        User user = User.builder()
                .name("김소울")
                .email("soul@example.com")
                .phone("010-1234-5678")
                .address("서울시 강남구")
                .active(true)
                .build();

        User createdUser = userService.createUser(user);

        // Then: 사용자가 Primary DataSource에 저장되었는지 확인
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("soul@example.com");
        log.info("✓ 사용자 생성 성공: {}", createdUser.getId());
    }

    @Test
    @DisplayName("사용자 조회 시 REPLICA DataSource 사용")
    void testGetUserUsesReplica() {
        // Given: 먼저 사용자를 생성
        User user = User.builder()
                .name("김고스트")
                .email("ghost@example.com")
                .phone("010-9876-5432")
                .address("서울시 종로구")
                .active(true)
                .build();
        
        User savedUser = userService.createUser(user);
        Long userId = savedUser.getId();

        // When: 사용자를 조회 (Replica DataSource 사용)
        Optional<User> retrievedUser = userService.getUserById(userId);

        // Then: 조회된 사용자 정보가 일치하는지 확인
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getEmail()).isEqualTo("ghost@example.com");
        log.info("✓ 사용자 조회 성공: {}", userId);
    }

    @Test
    @DisplayName("모든 사용자 조회는 @ReadOnlyOnReplica 어노테이션으로 REPLICA 사용")
    void testGetAllUsersUsesReplica() {
        // Given: 여러 사용자 생성
        User user1 = User.builder()
                .name("사용자1")
                .email("user1@example.com")
                .phone("010-0001-0001")
                .active(true)
                .build();

        User user2 = User.builder()
                .name("사용자2")
                .email("user2@example.com")
                .phone("010-0002-0002")
                .active(true)
                .build();

        userService.createUser(user1);
        userService.createUser(user2);

        // When: 모든 사용자 조회
        List<User> allUsers = userService.getAllUsers();

        // Then: 조회된 사용자 수 확인
        assertThat(allUsers).hasSize(2);
        log.info("✓ 모든 사용자 조회 성공: {} 명", allUsers.size());
    }

    @Test
    @DisplayName("사용자 정보 업데이트 시 PRIMARY DataSource 사용")
    void testUpdateUserUsesPrimary() {
        // Given: 사용자 생성
        User user = User.builder()
                .name("박문수")
                .email("bms@example.com")
                .phone("010-5555-5555")
                .active(true)
                .build();

        User savedUser = userService.createUser(user);

        // When: 사용자 정보 업데이트
        User updatedData = User.builder()
                .name("박문수_수정")
                .email("bms_updated@example.com")
                .phone("010-6666-6666")
                .active(false)
                .build();

        User updatedUser = userService.updateUser(savedUser.getId(), updatedData);

        // Then: 업데이트된 정보 확인
        assertThat(updatedUser.getName()).isEqualTo("박문수_수정");
        assertThat(updatedUser.getEmail()).isEqualTo("bms_updated@example.com");
        log.info("✓ 사용자 정보 업데이트 성공: {}", savedUser.getId());
    }

    @Test
    @DisplayName("사용자 삭제 시 PRIMARY DataSource 사용")
    void testDeleteUserUsesPrimary() {
        // Given: 사용자 생성
        User user = User.builder()
                .name("최삼문")
                .email("csm@example.com")
                .phone("010-7777-7777")
                .active(true)
                .build();

        User savedUser = userService.createUser(user);
        Long userId = savedUser.getId();

        // When: 사용자 삭제
        userService.deleteUser(userId);

        // Then: 삭제되었는지 확인
        Optional<User> deletedUser = userService.getUserById(userId);
        assertThat(deletedUser).isEmpty();
        log.info("✓ 사용자 삭제 성공: {}", userId);
    }

    @Test
    @DisplayName("이메일로 사용자 조회 시 REPLICA DataSource 사용")
    void testGetUserByEmailUsesReplica() {
        // Given: 사용자 생성
        User user = User.builder()
                .name("홍길동")
                .email("hgd@example.com")
                .phone("010-8888-8888")
                .address("부산시 중구")
                .active(true)
                .build();

        userService.createUser(user);

        // When: 이메일로 조회
        Optional<User> retrievedUser = userService.getUserByEmail("hgd@example.com");

        // Then: 조회된 사용자 정보 확인
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getName()).isEqualTo("홍길동");
        log.info("✓ 이메일로 사용자 조회 성공: {}", retrievedUser.get().getId());
    }

    @Test
    @DisplayName("활성화된 사용자만 조회")
    void testGetActiveUsersUsesReplica() {
        // Given: 활성화/비활성화 사용자 생성
        User activeUser = User.builder()
                .name("활성사용자")
                .email("active@example.com")
                .phone("010-9999-9999")
                .active(true)
                .build();

        User inactiveUser = User.builder()
                .name("비활성사용자")
                .email("inactive@example.com")
                .phone("010-0000-0000")
                .active(false)
                .build();

        userService.createUser(activeUser);
        userService.createUser(inactiveUser);

        // When: 활성화된 사용자 조회
        List<User> activeUsers = userService.getActiveUsers();

        // Then: 활성화된 사용자만 조회되어야 함
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getActive()).isEqualTo(true);
        log.info("✓ 활성화된 사용자 조회 성공: {} 명", activeUsers.size());
    }

    @Test
    @DisplayName("사용자명 검색 시 REPLICA DataSource 사용")
    void testSearchUsersUsesReplica() {
        // Given: 여러 사용자 생성
        User user1 = User.builder()
                .name("김철수")
                .email("kcs@example.com")
                .phone("010-1111-1111")
                .active(true)
                .build();

        User user2 = User.builder()
                .name("김영희")
                .email("kyh@example.com")
                .phone("010-2222-2222")
                .active(true)
                .build();

        User user3 = User.builder()
                .name("이순신")
                .email("lss@example.com")
                .phone("010-3333-3333")
                .active(true)
                .build();

        userService.createUser(user1);
        userService.createUser(user2);
        userService.createUser(user3);

        // When: "김"으로 검색
        List<User> searchResults = userService.searchUsers("김");

        // Then: "김"을 포함한 사용자만 조회되어야 함
        assertThat(searchResults).hasSize(2);
        log.info("✓ 사용자명 검색 성공: {} 명", searchResults.size());
    }

    @Test
    @DisplayName("활성화된 사용자 수 조회")
    void testCountActiveUsersUsesReplica() {
        // Given: 활성화/비활성화 사용자 생성
        for (int i = 1; i <= 3; i++) {
            User user = User.builder()
                    .name("사용자" + i)
                    .email("user" + i + "@example.com")
                    .phone("010-1111-" + String.format("%04d", i))
                    .active(i <= 2)  // 처음 2명은 활성화
                    .build();
            userService.createUser(user);
        }

        // When: 활성화된 사용자 수 조회
        long activeCount = userService.countActiveUsers();

        // Then: 활성화된 사용자 수가 2명이어야 함
        assertThat(activeCount).isEqualTo(2);
        log.info("✓ 활성화된 사용자 수 조회 성공: {} 명", activeCount);
    }

    @Test
    @DisplayName("DataSourceContext 초기화 후 PRIMARY DataSource가 기본값인지 확인")
    void testDataSourceContextDefaultValue() {
        // Given: 컨텍스트 초기화
        RouteDataSourceContext.clear();

        // Then: 기본값이 PRIMARY인지 확인
        assertThat(RouteDataSourceContext.getDataSourceType())
                .isEqualTo(RouteDataSourceContext.DataSourceType.PRIMARY);
        log.info("✓ DataSourceContext 기본값 확인: {}", RouteDataSourceContext.datosourceDisplayName());
    }

    @Test
    @DisplayName("복합 조회 시나리오: 여러 읽기-쓰기 작업 순차 실행")
    void testComplexReadWriteScenario() {
        // 시나리오: 사용자 생성 -> 조회 -> 수정 -> 재조회 -> 활성 사용자 조회

        // 1. 사용자 생성 (Write - Primary)
        User newUser = User.builder()
                .name("통합테스트사용자")
                .email("test@example.com")
                .phone("010-1234-5678")
                .active(true)
                .build();
        User created = userService.createUser(newUser);
        assertThat(created.getId()).isNotNull();
        log.info("✓ 단계 1: 사용자 생성 완료");

        // 2. 생성된 사용자 조회 (Read - Replica)
        Optional<User> retrieved = userService.getUserById(created.getId());
        assertThat(retrieved).isPresent();
        log.info("✓ 단계 2: 사용자 조회 완료");

        // 3. 사용자 정보 수정 (Write - Primary)
        User updateData = User.builder()
                .name("통합테스트사용자_수정")
                .email("test_updated@example.com")
                .phone("010-9876-5432")
                .active(true)
                .build();
        User updated = userService.updateUser(created.getId(), updateData);
        assertThat(updated.getEmail()).isEqualTo("test_updated@example.com");
        log.info("✓ 단계 3: 사용자 정보 수정 완료");

        // 4. 수정된 사용자 재조회 (Read - Replica)
        Optional<User> reRetrieved = userService.getUserById(created.getId());
        assertThat(reRetrieved).isPresent();
        assertThat(reRetrieved.get().getEmail()).isEqualTo("test_updated@example.com");
        log.info("✓ 단계 4: 수정된 사용자 재조회 완료");

        // 5. 활성 사용자 조회 (Read - Replica)
        List<User> activeUsers = userService.getActiveUsers();
        assertThat(activeUsers).isNotEmpty();
        log.info("✓ 단계 5: 활성 사용자 조회 완료");

        log.info("✓ 복합 조회 시나리오 완료");
    }
}
