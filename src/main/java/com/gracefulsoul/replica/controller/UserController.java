package com.gracefulsoul.replica.controller;

import com.gracefulsoul.replica.entity.User;
import com.gracefulsoul.replica.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User 관련 REST API Controller
 * 
 * CRUD 작업을 제공합니다.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 새 사용자를 생성합니다.
     *
     * @param user 생성할 사용자 정보
     * @return 생성된 사용자
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    /**
     * 모든 사용자를 조회합니다.
     *
     * @return 전체 사용자 목록
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * ID로 사용자를 조회합니다.
     *
     * @param id 사용자 ID
     * @return 사용자 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 사용자 정보를 업데이트합니다.
     *
     * @param id 사용자 ID
     * @param user 업데이트할 사용자 정보
     * @return 업데이트된 사용자
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    /**
     * 사용자를 삭제합니다.
     *
     * @param id 사용자 ID
     * @return 성공 응답
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 이메일로 사용자를 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 사용자 정보
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 활성화된 사용자 목록을 조회합니다.
     *
     * @return 활성화된 사용자 목록
     */
    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        return ResponseEntity.ok(userService.getActiveUsers());
    }

    /**
     * 사용자명으로 검색합니다.
     *
     * @param name 검색할 사용자명
     * @return 검색 결과 사용자 목록
     */
    @GetMapping("/search/{name}")
    public ResponseEntity<List<User>> searchUsers(@PathVariable String name) {
        return ResponseEntity.ok(userService.searchUsers(name));
    }

    /**
     * 활성화된 사용자 수를 반환합니다.
     *
     * @return 활성화된 사용자 수
     */
    @GetMapping("/count/active")
    public ResponseEntity<Long> countActiveUsers() {
        return ResponseEntity.ok(userService.countActiveUsers());
    }
}
