package com.gracefulsoul.replica.repository;

import com.gracefulsoul.replica.entity.UserLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UserLog Entity를 관리하는Repository
 * 
 * 주로 로그 입력과 조회 작업을 수행합니다.
 */
@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Long> {

    /**
     * 특정 사용자의 로그를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 로그 목록
     */
    @Query("SELECT ul FROM UserLog ul WHERE ul.userId = :userId ORDER BY ul.createdAt DESC")
    List<UserLog> findByUserId(@Param("userId") Long userId);

    /**
     * 특정 기간의 로그를 조회합니다.
     *
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 조회된 로그 목록
     */
    @Query("SELECT ul FROM UserLog ul WHERE ul.createdAt BETWEEN :startDate AND :endDate ORDER BY ul.createdAt DESC")
    List<UserLog> findLogsByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 액션의 로그 수를 반환합니다.
     *
     * @param action 액션 타입
     * @return 로그 수
     */
    long countByAction(String action);
}
