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
            log.atInfo()
                    .addKeyValue("event", "RATE_LIMIT_DECISION")
                    .addKeyValue("prefix", prefix)
                    .addKeyValue("identifier", identifier)
                    .addKeyValue("allowed", allowed)
                    .addKeyValue("limit", limit)
                    .addKeyValue("windowSize", windowSize)
                    .log("Rate limit decision");

            return allowed;

        } catch (Exception ex) {
            // 🔴 Redis failure
            log.atError()
                    .addKeyValue("event", "REDIS_FAILURE")
                    .addKeyValue("prefix", prefix)
                    .addKeyValue("identifier", identifier)
                    .addKeyValue("errorType", ex.getClass().getSimpleName())
                    .addKeyValue("error_message", ex.getMessage())
                    .log("Redis rate limit execution failed", ex);

            throw ex;
        }
    }

    private boolean checkFallback(String identifier, String prefix, long windowSize, long limit, Exception ex) {

        log.atError()
                .addKeyValue("event", "REDIS_CB_FALLBACK")
                .addKeyValue("prefix", prefix)
                .addKeyValue("identifier", identifier)
                .addKeyValue("fallback", "ALLOW")
                .addKeyValue("errorType", ex.getClass().getSimpleName())
                .addKeyValue("error_message", ex.getMessage())
                .log("Redis circuit breaker triggered - allowing request");

        return true; // fail open
    }
}