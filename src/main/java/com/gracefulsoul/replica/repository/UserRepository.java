package com.gracefulsoul.replica.repository;

import com.gracefulsoul.replica.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Entity를 관리하는 Repository
 * 
 * Spring Data JPA를 사용하여 CRUD 작업을 수행합니다.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일 주소로 사용자를 조회합니다.
     *
     * @param email 이메일 주소
     * @return 사용자 정보
     */
    Optional<User> findByEmail(String email);

    /**
     * 활성화된 사용자 목록을 조회합니다.
     *
     * @return 활성화된 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.createdAt DESC")
    List<User> findAllActiveUsers();

    /**
     * 이름으로 부분 검색합니다.
     *
     * @param name 검색할 사용자 이름
     * @return 검색 결과 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.name LIKE CONCAT('%', :name, '%')")
    List<User> searchByName(@Param("name") String name);

    /**
     * 활성화된 사용자 수를 반환합니다.
     *
     * @return 활성화된 사용자 수
     */
    long countByActive(Boolean active);
}
