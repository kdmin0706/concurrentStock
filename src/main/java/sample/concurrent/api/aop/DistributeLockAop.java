package sample.concurrent.api.aop;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributeLockAop {
  public static final String REDISSON_LOCK_PREFIX = "Lock:";

  private final RedissonClient redissonClient;
  private final AopForTransaction aopForTransaction;

  @Around("@annotation(sample.concurrent.api.aop.DistributedLock)")
  public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

    String key = REDISSON_LOCK_PREFIX +
        CustomSpringELParser.getDynamicValue(signature.getParameterNames(),
        joinPoint.getArgs(), distributedLock.key());
    RLock lock = redissonClient.getLock(key);

    try {
      boolean available = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(),
          distributedLock.timeUnit());
      if (!available) {
        return false;
      }

      return aopForTransaction.proceed(joinPoint);
    } catch (InterruptedException e) {
      throw new InterruptedException();
    } finally {
      try {
        lock.unlock();
      } catch (IllegalMonitorStateException e) {
        log.info("Redisson Lock Already Unlocked");
      }
    }
  }
}
