package com.project.realrank.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(distributedLock)")
    public Object handleConcurrency(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {

        String lockName = "lock:" + distributedLock.key();
        RLock lock = redissonClient.getLock(lockName);

        try {
            boolean available = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(),
                    distributedLock.timeUnit());

            if (!available) {
                log.warn("Redisson GetLock Timeout {}", lockName);
                throw new IllegalArgumentException();
            }

            log.info("Redisson GetLock {}", lockName);
            return aopForTransaction.proceed(joinPoint);
        } finally {
            try {
                lock.unlock();
            } catch (IllegalMonitorStateException e) {
                log.warn("Redisson Lock Already UnLock {}", lockName);
            }
        }
    }

}
