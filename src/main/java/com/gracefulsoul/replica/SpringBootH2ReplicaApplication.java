package com.gracefulsoul.replica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot H2 Replica Configuration Application
 * 
 * Primary-Replica DataSource 구성과 Transaction Routing을 구현한 
 * Spring Boot 애플리케이션입니다.
 * 
 * 활성화된 설정:
 * - @EnableAspectJAutoProxy: AOP를 활성화하여 DataSource 라우팅을 처리합니다
 * - @EnableTransactionManagement: 트랜잭션 관리를 활성화합니다
 */
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableTransactionManagement
public class SpringBootH2ReplicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootH2ReplicaApplication.class, args);
    }
}
