package com.my.firstbeat.web.service.recommemdation.lock.metric;

public final class LockMetricsConstants {

    private LockMetricsConstants() {}

    //메트릭 이름
    public static final String BASE_METRIC_NAME = "redis.lock.execution";
    public static final String METRIC_HOLD_TIME = BASE_METRIC_NAME + ".hold_time";
    public static final String METRIC_RETRIES = BASE_METRIC_NAME + ".retries";
    public static final String METRIC_TIMEOUTS = BASE_METRIC_NAME + ".timeouts";
    public static final String METRIC_ERRORS = BASE_METRIC_NAME + ".errors";

    //태그
    public static final String TAG_KEY = "key";
    public static final String TAG_OPERATION = "operation";
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_RETRY_COUNT = "retry_count";
    public static final String TAG_ERROR_TYPE = "error_type";

    //일반 작업
    public static final String OPERATION_LOCK = "lock";
    public static final String OPERATION_LOCK_ATTEMPT = "lock_attempt";
    public static final String OPERATION_LOCK_HOLD = "lock_hold";

    //백그라운드 작업
    public static final String OPERATION_BACKGROUND = "background";
    public static final String OPERATION_BACKGROUND_ATTEMPT = "background_attempt";
    public static final String OPERATION_BACKGROUND_HOLD = "background_hold";

    // 에러 타입
    public static final String ERROR_INTERRUPTED = "interrupted";
    public static final String ERROR_UNEXPECTED = "unexpected";
    public static final String ERROR_BACKGROUND_INTERRUPTED = "background_interrupted";
    public static final String ERROR_BACKGROUND_UNEXPECTED = "background_unexpected";
}
