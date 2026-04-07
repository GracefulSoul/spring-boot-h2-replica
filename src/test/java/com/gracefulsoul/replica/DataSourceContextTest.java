package com.gracefulsoul.replica;

import com.gracefulsoul.replica.entity.User;
import com.gracefulsoul.replica.repository.UserRepository;
import com.gracefulsoul.replica.routing.RouteDataSourceContext;
import com.gracefulsoul.replica.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * DataSourceContext 단위 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class DataSourceContextTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("DataSourceContext 기본값이 PRIMARY인지 확인")
    void testDefaultDataSourceContext() {
        RouteDataSourceContext.clear();
        assertThat(RouteDataSourceContext.getDataSourceType())
                .isEqualTo(RouteDataSourceContext.DataSourceType.PRIMARY);
    }

    @Test
    @DisplayName("DataSourceContext를 REPLICA로 설정할 수 있는지 확인")
    void testSetReplicaDataSourceContext() {
        RouteDataSourceContext.setReplica();
        assertThat(RouteDataSourceContext.getDataSourceType())
                .isEqualTo(RouteDataSourceContext.DataSourceType.REPLICA);
    }

    @Test
    @DisplayName("DataSourceContext를 PRIMARY로 설정할 수 있는지 확인")
    void testSetPrimaryDataSourceContext() {
        RouteDataSourceContext.setReplica();
        RouteDataSourceContext.setPrimary();
        assertThat(RouteDataSourceContext.getDataSourceType())
                .isEqualTo(RouteDataSourceContext.DataSourceType.PRIMARY);
    }

    @Test
    @DisplayName("DataSourceContext의 표시 이름을 확인")
    void testDataSourceDisplayName() {
        RouteDataSourceContext.setPrimary();
        assertThat(RouteDataSourceContext.datosourceDisplayName()).isEqualTo("PRIMARY");

        RouteDataSourceContext.setReplica();
        assertThat(RouteDataSourceContext.datosourceDisplayName()).isEqualTo("REPLICA");
    }

    @Nested
    @DisplayName("UserService DataSource 라우팅 테스트")
    class UserServiceDataSourceRoutingTest {

        @BeforeEach
        void setUp() {
            RouteDataSourceContext.clear();
            userRepository.deleteAll();
        }

        @Test
        @DisplayName("createUser: Primary DataSource 사용")
        void testCreateUserDataSourceRouting() {
            User user = User.builder()
                    .name("테스트사용자")
                    .email("test@example.com")
                    .phone("010-1234-5678")
                    .active(true)
                    .build();

            User created = userService.createUser(user);
            assertThat(created.getId()).isNotNull();
        }

        @Test
        @DisplayName("getUserById: Replica DataSource 사용 (readOnly=true)")
        void testGetUserByIdDataSourceRouting() {
            User user = User.builder()
                    .name("테스트사용자")
                    .email("test@example.com")
                    .phone("010-1234-5678")
                    .active(true)
                    .build();

            User created = userService.createUser(user);
            Optional<User> retrieved = userService.getUserById(created.getId());
            assertThat(retrieved).isPresent();
        }

        @Test
        @DisplayName("getUserByEmail: Replica DataSource 사용 (@ReadOnlyOnReplica)")
        void testGetUserByEmailDataSourceRouting() {
            User user = User.builder()
                    .name("테스트사용자")
                    .email("test@example.com")
                    .phone("010-1234-5678")
                    .active(true)
                    .build();

            userService.createUser(user);
            Optional<User> retrieved = userService.getUserByEmail("test@example.com");
            assertThat(retrieved).isPresent();
        }

        @Test
        @DisplayName("updateUser: Primary DataSource 사용")
        void testUpdateUserDataSourceRouting() {
            User user = User.builder()
                    .name("테스트사용자")
                    .email("test@example.com")
                    .phone("010-1234-5678")
                    .active(true)
                    .build();

            User created = userService.createUser(user);

            User updateData = User.builder()
                    .name("수정된사용자")
                    .email("updated@example.com")
                    .phone("010-9876-5432")
                    .active(true)
                    .build();

            User updated = userService.updateUser(created.getId(), updateData);
            assertThat(updated.getName()).isEqualTo("수정된사용자");
        }

        @Test
        @DisplayName("deleteUser: Primary DataSource 사용")
        void testDeleteUserDataSourceRouting() {
            User user = User.builder()
                    .name("테스트사용자")
                    .email("test@example.com")
                    .phone("010-1234-5678")
                    .active(true)
                    .build();

            User created = userService.createUser(user);
            userService.deleteUser(created.getId());
            Optional<User> deleted = userService.getUserById(created.getId());
            assertThat(deleted).isEmpty();
        }
    }
}
