package com.gracefulsoul.replica.aspect;

import com.gracefulsoul.replica.routing.ReadOnlyOnReplica;
import com.gracefulsoul.replica.routing.RouteDataSourceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * DataSource 라우팅을 처리하는 AOP Aspect
 * 
 * @ReadOnlyOnReplica 어노테이션이 붙은 메서드는 Replica DataSource를 사용하고,
 * @Transactional(readOnly=true)가 붙은 메서드도 Replica DataSource를 사용합니다.
 * 
 * 메커니즘:
 * 1. Spring Transaction Aspect보다 우선순위가 높은 @Around advice로 작동
 * 2. 트랜잭션 시작 전에 DataSource를 결정합니다
 * 3. 메서드 실행 중: 설정된 DataSource가 사용됩니다
 * 4. 메서드 완료 후 원래 상태로 복원합니다
 */
@Aspect
@Component
@Slf4j
public class DataSourceRoutingAspect {

    private static final String POINTCUT_READ_ONLY_ON_REPLICA = "@annotation(com.gracefulsoul.replica.routing.ReadOnlyOnReplica)";
    private static final String POINTCUT_TRANSACTIONAL_READ_ONLY = "@annotation(org.springframework.transaction.annotation.Transactional) && args()";

    /**
     * @ReadOnlyOnReplica 어노테이션이 붙은 메서드를 처리합니다.
     * 메서드 실행 전에 Replica로 전환하고, 완료 후 원래 상태로 복원합니다.
     *
     * @param joinPoint 대상 메서드 정보
     * @param readOnlyOnReplica @ReadOnlyOnReplica 어노테이션
     * @return 메서드 실행 결과
     * @throws Throwable 메서드 실행 중 발생한 예외
     */
    @Around(POINTCUT_READ_ONLY_ON_REPLICA)
    public Object aroundReadOnlyOnReplica(ProceedingJoinPoint joinPoint, ReadOnlyOnReplica readOnlyOnReplica) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        RouteDataSourceContext.DataSourceType originalType = RouteDataSourceContext.getDataSourceType();
        RouteDataSourceContext.setReplica();
        log.debug("메서드 시작: {}.{} -> Replica DataSource 선택됨", className, methodName);
        
        try {
            return joinPoint.proceed();
        } finally {
            RouteDataSourceContext.setDataSourceType(originalType);
            log.debug("메서드 완료: {}.{} -> {} DataSource로 복원됨", className, methodName, originalType.getName());
        }
    }

    /**
     * @Transactional(readOnly=true) 어노테이션이 붙은 메서드를 처리합니다.
     * readOnly=true인 경우 Replica로 전환하고, 완료 후 원래 상태로 복원합니다.
     *
     * @param joinPoint 대상 메서드 정보
     * @param transactional @Transactional 어노테이션
     * @return 메서드 실행 결과
     * @throws Throwable 메서드 실행 중 발생한 예외
     */
    @Around("@annotation(transactional)")
    public Object aroundTransactionalMethod(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        // readOnly=false인 경우는 Primary를 기본값으로 사용하므로 처리하지 않음
        if (!transactional.readOnly()) {
            return joinPoint.proceed();
        }
        
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        RouteDataSourceContext.DataSourceType originalType = RouteDataSourceContext.getDataSourceType();
        RouteDataSourceContext.setReplica();
        log.debug("메서드 시작: {}.{} -> Transactional(readOnly=true) Replica DataSource 선택됨", className, methodName);
        
        try {
            return joinPoint.proceed();
        } finally {
            RouteDataSourceContext.setDataSourceType(originalType);
            log.debug("메서드 완료: {}.{} -> {} DataSource로 복원됨", className, methodName, originalType.getName());
        }
    }
}
