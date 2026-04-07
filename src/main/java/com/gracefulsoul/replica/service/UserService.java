package com.gracefulsoul.replica.service;

import com.gracefulsoul.replica.entity.User;
import com.gracefulsoul.replica.repository.UserRepository;
import com.gracefulsoul.replica.routing.ReadOnlyOnReplica;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * User 관련 비즈니스 로직을 처리하는 Service
 * 
 * Write 작업: Primary DataSource 사용
 * Read 작업: @ReadOnlyOnReplica 또는 @Transactional(readOnly=true) 사용하여 Replica DataSource 사용
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * 새 사용자를 생성합니다 (Primary DataSource 사용).
     *
     * @param user 생성할 사용자 정보
     * @return 생성된 사용자
     */
    @Transactional
    public User createUser(User user) {
        log.info("새로운 사용자 생성: {}", user.getEmail());
        return userRepository.save(user);
    }

    /**
     * 사용자 정보를 업데이트합니다 (Primary DataSource 사용).
     *
     * @param id 사용자 ID
     * @param user 업데이트할 사용자 정보
     * @return 업데이트된 사용자
     */
    @Transactional
    public User updateUser(Long id, User user) {
        log.info("사용자 정보 업데이트: {}", id);
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setName(user.getName());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setPhone(user.getPhone());
                    existingUser.setAddress(user.getAddress());
                    existingUser.setActive(user.getActive());
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));
    }

    /**
     * 사용자를 삭제합니다 (Primary DataSource 사용).
     *
     * @param id 사용자 ID
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("사용자 삭제: {}", id);
        userRepository.deleteById(id);
    }

    /**
     * 모든 사용자를 조회합니다 (Replica DataSource 사용).
     *
     * @return 전체 사용자 목록
     */
    @ReadOnlyOnReplica("전체 사용자 조회 (Replica 사용)")
    public List<User> getAllUsers() {
        log.info("전체 사용자 조회");
        return userRepository.findAll();
    }

    /**
     * ID로 사용자를 조회합니다 (Replica DataSource 사용).
     *
     * @param id 사용자 ID
     * @return 사용자 정보
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        log.info("사용자 조회: {}", id);
        return userRepository.findById(id);
    }

    /**
     * 이메일로 사용자를 조회합니다 (Replica DataSource 사용).
     *
     * @param email 사용자 이메일
     * @return 사용자 정보
     */
    @ReadOnlyOnReplica("이메일로 사용자 조회 (Replica 사용)")
    public Optional<User> getUserByEmail(String email) {
        log.info("이메일로 사용자 조회: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * 활성화된 사용자 목록을 조회합니다 (Replica DataSource 사용).
     *
     * @return 활성화된 사용자 목록
     */
    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        log.info("활성화된 사용자 조회");
        return userRepository.findAllActiveUsers();
    }

    /**
     * 사용자명으로 검색합니다 (Replica DataSource 사용).
     *
     * @param name 검색할 사용자명
     * @return 검색 결과 사용자 목록
     */
    @ReadOnlyOnReplica("사용자명 검색 (Replica 사용)")
    public List<User> searchUsers(String name) {
        log.info("사용자명 검색: {}", name);
        return userRepository.searchByName(name);
    }

    /**
     * 활성화된 사용자 수를 반환합니다 (Replica DataSource 사용).
     *
     * @return 활성화된 사용자 수
     */
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        log.info("활성화된 사용자 수 조회");
        return userRepository.countByActive(true);
    }
}
