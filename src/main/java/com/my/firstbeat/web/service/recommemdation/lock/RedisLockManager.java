package com.my.firstbeat.web.service.recommemdation.lock;

import com.my.firstbeat.web.service.recommemdation.property.LockProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisLockManager {

    private final RedissonClient redissonClient;

    private final LockProperties properties;

    //락 획득 후 작업 실행
    public <T> Optional<T> executeWithLock(Long key, Supplier<T> action){
        RLock lock = redissonClient.getLock(properties.getKeyPrefix() + key);
        long startTime = System.currentTimeMillis();

        try{
            //현재는 3초 동안 락 획득 시도하고, 획득하면 10초 동안 점유
            //내부적으로 워치독 메커니즘에 따라 작업이 10초 이상 걸릴 경우 자동으로 시간 연장
            boolean isLocked = lock.tryLock(properties.getWaitTime(), properties.getLeaseTime(), TimeUnit.SECONDS);
            if(!isLocked){
                return Optional.empty();
            }
            return Optional.ofNullable(action.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("key: {}에 의해 락 interrupted. 원인: {}", key, e.getMessage());
            return Optional.empty();
        } catch (Exception e){
            log.error("key: {}가 락 획득 시도 중 예기치 못한 오류 발생. 원인: {}", key, e.getMessage(), e);
            throw e;
        } finally {
            if(lock.isHeldByCurrentThread()){ //현재 스레드가 락을 점유하고 있는 경우에만 해제
                long duration = System.currentTimeMillis() - startTime;
                log.info("락 작업 소요 시간: {}ms, 유저 ID: {}", duration, key);
                lock.unlock();
            }
        }
    }

    //void 작업을 위한 오버로드
    public boolean executeWithLock(Long key, Runnable action){
        RLock lock = redissonClient.getLock(properties.getKeyPrefix() + key);

        try{
            boolean isLocked = lock.tryLock(properties.getWaitTime(), properties.getLeaseTime(), TimeUnit.SECONDS);
            if(!isLocked){
                return false;
            }
            action.run();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("key: {}에 의해 락 interrupted. 원인: {}", key, e.getMessage());
            return false;
        }catch (Exception e){
            log.error("key: {}가 락 획득 시도 중 예기치 못한 오류 발생. 원인: {}", key, e.getMessage(), e);
            throw e;
        } finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }


}
