package com.gracefulsoul.replica.config;

import com.gracefulsoul.replica.routing.ReadWriteRoutingDataSource;
import com.gracefulsoul.replica.routing.RouteDataSourceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Primary-Replica DataSource 설정 클래스
 * 
 * 두 개의 H2 DataSource (Primary, Replica)를 생성하고,
 * ReadWriteRoutingDataSource를 통해 자동으로 라우팅하는 설정입니다.
 * 
 * 구조:
 * 1. Primary DataSource: Write 작업 (INSERT, UPDATE, DELETE)
 * 2. Replica DataSource: Read 작업 (SELECT)
 * 3. ReadWriteRoutingDataSource: 들어오는 요청을 적절한 DataSource로 라우팅
 */
@Configuration
@Slf4j
public class DataSourceConfig {

    /**
     * Primary DataSource Bean 생성
     * 
     * application.yml의 spring.datasource.primary 설정을 사용합니다.
     * Write 작업을 위한 메인 데이터베이스입니다.
     *
     * @return Primary DataSource
     */
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        log.info("Primary DataSource 초기화 중...");
        return DataSourceBuilder.create().build();
    }

    /**
     * Replica DataSource Bean 생성
     * 
     * application.yml의 spring.datasource.replica 설정을 사용합니다.
     * Read 작업을 위한 복제 데이터베이스입니다.
     *
     * @return Replica DataSource
     */
    @Bean(name = "replicaDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.replica")
    public DataSource replicaDataSource() {
        log.info("Replica DataSource 초기화 중...");
        return DataSourceBuilder.create().build();
    }

    /**
     * ReadWriteRoutingDataSource Bean 생성 (Primary Bean)
     * 
     * Primary와 Replica DataSource를 등록하고,
     * 각 요청에 따라 자동으로 라우팅합니다.
     *
     * @param primaryDataSource Primary DataSource
     * @param replicaDataSource Replica DataSource
     * @return 라우팅 DataSource
     */
    @Bean
    @Primary
    public DataSource routingDataSource(
            DataSource primaryDataSource,
            DataSource replicaDataSource) {
        
        ReadWriteRoutingDataSource routingDataSource = new ReadWriteRoutingDataSource();
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(RouteDataSourceContext.DataSourceType.PRIMARY, primaryDataSource);
        targetDataSources.put(RouteDataSourceContext.DataSourceType.REPLICA, replicaDataSource);
        
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource);
        
        log.info("ReadWriteRoutingDataSource 설정 완료");
        
        try {
            log.info("  - PRIMARY: {}", primaryDataSource.getConnection().getMetaData().getURL());
            log.info("  - REPLICA: {}", replicaDataSource.getConnection().getMetaData().getURL());
        } catch (SQLException e) {
            log.warn("DataSource URL 로깅 중 오류 발생: {}", e.getMessage());
        }
        
        return routingDataSource;
    }

    /**
     * TransactionManager Bean 생성
     * 
     * 라우팅 DataSource를 기반으로 트랜잭션을 관리합니다.
     *
     * @param routingDataSource 라우팅 DataSource
     * @return PlatformTransactionManager
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource routingDataSource) {
        return new DataSourceTransactionManager(routingDataSource);
    }

    /**
     * JPA Vendor Adapter Bean 생성
     * Hibernate를 JPA 구현체로 사용합니다.
     *
     * @return JpaVendorAdapter
     */
    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }
}
