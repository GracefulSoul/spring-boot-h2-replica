package com.gracefulsoul.replica.routing;

import lombok.Getter;

/**
 * 현재 스레드의 DataSource를 관리하는 Context 클래스
 * ThreadLocal을 사용하여 각 스레드별로 독립적인 DataSource 정보를 유지
 */
public class RouteDataSourceContext {

    public enum DataSourceType {
        PRIMARY("PRIMARY", "Write 작업용 Primary DataSource"),
        REPLICA("REPLICA", "Read 작업용 Replica DataSource");

        @Getter
        private final String name;
        
        @Getter
        private final String description;

        DataSourceType(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    private static final ThreadLocal<DataSourceType> contextHolder = ThreadLocal.withInitial(() -> DataSourceType.PRIMARY);

    /**
     * DataSource 타입을 설정합니다 (Primary 또는 Replica).
     *
     * @param dataSourceType 설정할 DataSource 타입
     */
    public static void setDataSourceType(DataSourceType dataSourceType) {
        contextHolder.set(dataSourceType);
    }

    /**
     * 현재 스레드에 설정된 DataSource 타입을 반환합니다.
     *
     * @return 현재 DataSource 타입
     */
    public static DataSourceType getDataSourceType() {
        return contextHolder.get();
    }

    /**
     * 현재 스레드의 DataSource 타입을 Primary로 설정합니다.
     */
    public static void setPrimary() {
        setDataSourceType(DataSourceType.PRIMARY);
    }

    /**
     * 현재 스레드의 DataSource 타입을 Replica로 설정합니다.
     */
    public static void setReplica() {
        setDataSourceType(DataSourceType.REPLICA);
    }

    /**
     * 현재 스레드의 DataSource 컨텍스트를 초기화합니다 (기본값: PRIMARY).
     */
    public static void clear() {
        contextHolder.set(DataSourceType.PRIMARY);
    }

    /**
     * MDC(Mapped Diagnostic Context) 정보를 반환합니다.
     * 로그에 현재 DataSource 정보를 포함시킬 때 사용.
     *
     * @return DataSource 타입 문자열
     */
    public static String datosourceDisplayName() {
        return getDataSourceType().getName();
    }
}
