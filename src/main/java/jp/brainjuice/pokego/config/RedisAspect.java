package jp.brainjuice.pokego.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class RedisAspect {

    @Around("execution(* org.springframework.data.redis.core.RedisTemplate.*(..))")
    public Object logRedisOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        // メソッド名と引数を取得
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.debug("Redis operation: {} with args: {}", methodName, args);

        Object result;
        try {
            result = joinPoint.proceed();
            log.debug("Redis operation: {} completed with result: {}", methodName, result);
        } catch (Throwable e) {
            log.error("Redis operation: {} failed", methodName, e);
            throw e;
        }

        return result;
    }
}
