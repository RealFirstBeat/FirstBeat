package com.my.firstbeat.web.service.recommemdation.lock.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class LockExecutionMetrics {
    private final MeterRegistry registry;
    public void recordLockAttempt(Long key, boolean success, long durationMs, int retryCount, boolean isBackground) {
        String operation = isBackground ?
                LockMetricsConstants.OPERATION_BACKGROUND_ATTEMPT :
                LockMetricsConstants.OPERATION_LOCK_ATTEMPT;

        Timer timer = Timer.builder(LockMetricsConstants.BASE_METRIC_NAME)
                .tag(LockMetricsConstants.TAG_KEY, String.valueOf(key))
                .tag(LockMetricsConstants.TAG_OPERATION, operation)
                .tag(LockMetricsConstants.TAG_SUCCESS, String.valueOf(success))
                .tag(LockMetricsConstants.TAG_RETRY_COUNT, String.valueOf(retryCount))
                .register(registry);

        timer.record(durationMs, TimeUnit.MILLISECONDS);

        registry.counter(LockMetricsConstants.METRIC_RETRIES,
                        LockMetricsConstants.TAG_KEY, String.valueOf(key),
                        LockMetricsConstants.TAG_OPERATION, operation,
                        LockMetricsConstants.TAG_RETRY_COUNT, String.valueOf(retryCount))
                .increment();
    }

    public void recordLockDuration(Long key, long durationMs, boolean isBackground) {
        String operation = isBackground ?
                LockMetricsConstants.OPERATION_BACKGROUND_HOLD :
                LockMetricsConstants.OPERATION_LOCK_HOLD;

        Timer timer = Timer.builder(LockMetricsConstants.METRIC_HOLD_TIME)
                .tag(LockMetricsConstants.TAG_KEY, String.valueOf(key))
                .tag(LockMetricsConstants.TAG_OPERATION, operation)
                .register(registry);

        timer.record(durationMs, TimeUnit.MILLISECONDS);
    }


    public void recordLockTimeout(Long key, boolean isBackground) {
        String operation = isBackground ?
                LockMetricsConstants.OPERATION_BACKGROUND :
                LockMetricsConstants.OPERATION_LOCK;

        registry.counter(LockMetricsConstants.METRIC_TIMEOUTS,
                        LockMetricsConstants.TAG_KEY, String.valueOf(key),
                        LockMetricsConstants.TAG_OPERATION, operation)
                .increment();
    }

    public void recordLockError(Long key, String errorType, boolean isBackground) {
        String operation = isBackground ?
                LockMetricsConstants.OPERATION_BACKGROUND :
                LockMetricsConstants.OPERATION_LOCK;

        registry.counter(LockMetricsConstants.METRIC_ERRORS,
                        LockMetricsConstants.TAG_KEY, String.valueOf(key),
                        LockMetricsConstants.TAG_OPERATION, operation,
                        LockMetricsConstants.TAG_ERROR_TYPE, errorType)
                .increment();
    }
}
