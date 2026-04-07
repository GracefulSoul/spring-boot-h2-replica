package com.gracefulsoul.replica.routing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Replica DataSource 사용을 지정하는 Annotation
 * 
 * 이 어노테이션이 메서드에 붙으면 해당 메서드의 트랜잭션 동안
 * Replica DataSource에서만 데이터를 읽습니다 (읽기 전용).
 * 
 * 주로 다음과 같이 사용됩니다:
 * - 조회 메서드
 * - 통계/분석 쿼리
 * - 리포팅 기능
 * 
 * 주의: 이 어노테이션을 붙인 메서드에서 INSERT/UPDATE/DELETE를 시도하면
 * 데이터베이스 오류가 발생할 수 있습니다.
 * 
 * @see com.gracefulsoul.replica.aspect.DataSourceRoutingAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOnlyOnReplica {
    /**
     * 이 어노테이션의 설명을 지정합니다 (선택 사항).
     *
     * @return 설명 문자열
     */
    String value() default "Replica DataSource에서 읽기 작업 수행";
}
