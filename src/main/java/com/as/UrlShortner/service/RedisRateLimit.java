package com.as.UrlShortner.service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class RedisRateLimit {
    private StringRedisTemplate redisTemplate;
    private DefaultRedisScript<String> redisScript;

    @CircuitBreaker(name = "redis", fallbackMethod = "checkFallback")
    public boolean check(String identifier, String prefix, long windowSize, long limit) {

        long now = System.currentTimeMillis() / 1000;
        long currentWindowStartTime = (now / windowSize) * (windowSize);
        long prevWindowStartTime = currentWindowStartTime - windowSize;

        String curKey = "rate_limit:" + prefix + ":" + identifier + ":" + currentWindowStartTime;
        String prevKey = "rate_limit:" + prefix + ":" + identifier + ":" + prevWindowStartTime;
        List<String> keys = Arrays.asList(curKey, prevKey);

        try {
            String result = redisTemplate.execute(redisScript, keys,
                    String.valueOf(now),
                    String.valueOf(windowSize),
                    String.valueOf(limit));

            boolean allowed = "1".equals(result);

            // ✅ Decision log
            log.info("event=RATE_LIMIT_DECISION prefix={} identifier={} allowed={} limit={} windowSize={}",prefix,identifier,allowed,limit,windowSize);
            return allowed;

        } catch (Exception ex) {
            // 🔴 Redis failure
            log.info(
                    "event=REDIS_FAILURE prefix={} identifier={} errorType={} error_message={} - Redis rate limit execution failed",
                    prefix,
                    identifier,
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    ex
            );

            throw ex;
        }
    }

    private boolean checkFallback(String identifier, String prefix, long windowSize, long limit, Exception ex) {

        log.info(
                "event=REDIS_CB_FALLBACK prefix={} identifier={} fallback=ALLOW errorType={} error_message={} - Redis circuit breaker triggered - allowing request",
                prefix,
                identifier,
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex
        );

        return true; // fail open
    }
}