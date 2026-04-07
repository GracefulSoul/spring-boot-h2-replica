package com.gracefulsoul.replica.routing;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Read-Write 트래잭션 라우팅을 구현하는 라우팅 DataSource
 * 
 * AbstractRoutingDataSource를 상속받아 determineCurrentLookupKey() 메서드를 구현합니다.
 * RouteDataSourceContext에 저장된 DataSource 타입에 따라 
 * Primary 또는 Replica DataSource로 자동 라우팅됩니다.
 * 
 * 라우팅 규칙:
 * - Write 작업 (INSERT, UPDATE, DELETE): PRIMARY
 * - Read 작업 (SELECT): REPLICA
 * - Transaction Read-Only 모드: REPLICA
 */
public class ReadWriteRoutingDataSource extends AbstractRoutingDataSource {

    /**
     * 현재 트랜잭션에 사용할 DataSource를 결정합니다.
     * 
     * 이 메서드는 각 DB 접근 시점에 호출되어 
     * RouteDataSourceContext에 저장된 값을 기반으로 
     * PRIMARY 또는 REPLICA를 결정합니다.
     *
     * @return DataSource 키값 (PRIMARY 또는 REPLICA)
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return RouteDataSourceContext.getDataSourceType();
    }

    /**
     * 현재 라우팅 대상 DataSource 타입을 로그 형식으로 반환합니다.
     *
     * @return DataSource 타입 (PRIMARY/REPLICA)
     */
    public String getCurrentDataSourceType() {
        return RouteDataSourceContext.datosourceDisplayName();
    }
}
