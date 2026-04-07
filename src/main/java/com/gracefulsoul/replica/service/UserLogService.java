package com.gracefulsoul.replica.service;

import com.gracefulsoul.replica.entity.UserLog;
import com.gracefulsoul.replica.repository.UserLogRepository;
import com.gracefulsoul.replica.routing.ReadOnlyOnReplica;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UserLog 관련 비즈니스 로직을 처리하는 Service
 * 
 * Write 작업: Primary DataSource 사용
 * Read 작업: @ReadOnlyOnReplica 또는 @Transactional(readOnly=true) 사용하여 Replica DataSource 사용
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserLogService {

    private final UserLogRepository userLogRepository;

    /**
     * 사용자 로그를 기록합니다 (Primary DataSource 사용).
     * 
     * 주로 INSERT 작업이므로 Primary DataSource를 사용합니다.
     * 일반적으로 쓰기 작업이므로 @Transactional (readOnly = false, 기본값) 사용합니다.
     *
     * @param log 기록할 로그 정보
     * @return 저장된 로그
     */
    @Transactional
    public UserLog createLog(UserLog userLog) {
        log.info("사용자 로그 기록: userId={}, action={}", userLog.getUserId(), userLog.getAction());
        return userLogRepository.save(userLog);
    }

    /**
     * 특정 사용자의 모든 로그를 조회합니다 (Replica DataSource 사용).
     *
     * @param userId 사용자 ID
     * @return 사용자 로그 목록
     */
    @ReadOnlyOnReplica("사용자 로그 조회 (Replica 사용)")
    public List<UserLog> getLogsByUserId(Long userId) {
        log.info("사용자 로그 조회: userId={}", userId);
        return userLogRepository.findByUserId(userId);
    }

    /**
     * 지정된 기간의 로그를 조회합니다 (Replica DataSource 사용).
     *
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 조회된 로그 목록
     */
    @Transactional(readOnly = true)
    public List<UserLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("기간별 로그 조회: {} ~ {}", startDate, endDate);
        return userLogRepository.findLogsByDateRange(startDate, endDate);
    }

    /**
     * 모든 로그를 조회합니다 (Replica DataSource 사용).
     *
     * @return 전체 로그 목록
     */
    @ReadOnlyOnReplica("전체 로그 조회 (Replica 사용)")
    public List<UserLog> getAllLogs() {
        log.info("전체 로그 조회");
        return userLogRepository.findAll();
    }

    /**
     * ID로 특정 로그를 조회합니다 (Replica DataSource 사용).
     *
     * @param id 로그 ID
     * @return 로그 정보
     */
    @Transactional(readOnly = true)
    public UserLog getLogById(Long id) {
        log.info("로그 조회: id={}", id);
        return userLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("로그를 찾을 수 없습니다: " + id));
    }

    /**
     * 특정 액션의 로그 수를 반환합니다 (Replica DataSource 사용).
     *
     * @param action 액션 타입
     * @return 로그 수
     */
    @ReadOnlyOnReplica("액션별 로그 수 조회 (Replica 사용)")
    public long countByAction(String action) {
        log.info("액션별 로그 수 조회: action={}", action);
        return userLogRepository.countByAction(action);
    }
}
