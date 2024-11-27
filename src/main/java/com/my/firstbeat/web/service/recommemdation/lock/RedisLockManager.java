package com.my.firstbeat.web.service.recommemdation.lock;

import com.my.firstbeat.web.service.recommemdation.property.LockProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.transaction.internal.SynchronizationRegistryStandardImpl;
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

    //애플리케이션 레벨에서의 재시도 메커니즘 적용한 락 획득 후 작업 실행
    public <T> Optional<T> executeWithLockWithRetry(Long key, Supplier<T> action){
        int attempts = 0;
        long backoffMs = properties.getInitialBackOffMs();

        while(attempts < properties.getMaxAttempts()){ //최대 횟수 동안 락 획득 재시도 (대기 시간을 넘어선 경우)
            RLock lock = redissonClient.getLock(properties.getKeyPrefix() + key);
            long startTime = System.currentTimeMillis();

            try{
                boolean isLocked = lock.tryLock(properties.getWaitTime(), properties.getLeaseTime(), TimeUnit.SECONDS);
                if(isLocked) {
                    return Optional.ofNullable(action.get());
                }

                attempts++;

                if(attempts < properties.getMaxAttempts()){
                    log.warn("keyy: {}에 대한 락 획득 실패. 시도 횟수: {}/{}. {}ms 후 재시도", key, attempts, properties.getMaxAttempts(), backoffMs);
                    Thread.sleep(backoffMs);
                    backoffMs *= properties.getBackOffMultiplier();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("key: {}에 대한 락 처리 중 인터럽트 발생. 시도 횟수: {}/{}. 원인: {}",
                        key, attempts, properties.getMaxAttempts(), e.getMessage(), e);
                return Optional.empty();
            } catch (Exception e){
                log.error("key: {}에 대한 락 처리 중 예기치 못한 오류 발생. 시도 횟수: {}/{}. 원인: {}",
                        key, attempts, properties.getMaxAttempts(), e.getMessage(), e);
                throw e;
            } finally {
                if(lock.isHeldByCurrentThread()){
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("락 작업 완료. 소요 시간: {}ms, 사용자 ID: {}, 시도 횟수: {}/{}",
                            duration, key, attempts + 1, properties.getMaxAttempts());
                    lock.unlock();
                }
            }
        }
        log.error("key: {}에 대한 락 획득 실패. 최대 시도 횟수({}) 도달", key, attempts);
        return Optional.empty();
    }



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
            log.error("key: {}에 대한 락 처리 중 인터럽트 발생. 원인: {}", key, e.getMessage());
            return Optional.empty();
        } catch (Exception e){
            log.error("key: {}가 락 처리 중 예기치 못한 오류 발생. 원인: {}", key, e.getMessage(), e);
            throw e;
        } finally {
            if(lock.isHeldByCurrentThread()){ //현재 스레드가 락을 점유하고 있는 경우에만 해제
                long duration = System.currentTimeMillis() - startTime;
                log.info("락 작업 소요 시간: {}ms, 유저 ID: {}", duration, key);
                lock.unlock();
            }
        }
    }

    //백그라운드 작업용 void 오버로드 + 재시도 로직 추가
    public boolean executeWithLockForBackground(Long key, Runnable action){
        int attempts = 0;
        long backoffMs = properties.getBackground().getInitialBackOffMs();

        while(attempts < properties.getBackground().getMaxAttempts()){
            RLock lock = redissonClient.getLock(properties.getKeyPrefix() + key);

            try{
                boolean isLocked = lock.tryLock(properties.getWaitTime(), properties.getLeaseTime(), TimeUnit.SECONDS);
                if(isLocked){
                    action.run();
                    return true;
                }

                attempts++;
                if(attempts < properties.getBackground().getMaxAttempts()){
                    log.warn("키: {}에 대한 백그라운드 작업 락 획득 실패. 시도 횟수: {}/{}. {}ms 후 재시도",
                            key, attempts, properties.getBackground().getMaxAttempts(), backoffMs);
                    Thread.sleep(backoffMs);
                    backoffMs *= properties.getBackground().getBackOffMultiplier();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("키: {}에 대한 백그라운드 작업 락 처리 중 예기치 못한 오류 발생. 시도 횟수: {}/{}",
                        key, attempts, properties.getBackground().getMaxAttempts(), e);
                return false;
            } catch (Exception e){
                log.error("키: {}에 대한 백그라운드 작업 락 처리 중 예기치 못한 오류 발생. 시도 횟수: {}/{}",
                        key, attempts, properties.getBackground().getMaxAttempts(), e);
                throw e;
            } finally {
                if(lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }
        }
        log.error("키: {}에 대한 백그라운드 작업 락 획득 실패. 최대 시도 횟수({}) 도달", key, properties.getBackground().getMaxAttempts());
        return false;
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
