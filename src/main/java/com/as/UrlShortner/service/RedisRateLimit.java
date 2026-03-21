package com.as.UrlShortner.service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Component
@AllArgsConstructor
@Slf4j
public class RedisRateLimit {
    private StringRedisTemplate redisTemplate;
    private DefaultRedisScript<String> redisScript;

    @CircuitBreaker(name="redis", fallbackMethod = "checkFallback")
    public boolean check(String identifier, String prefix, long windowSize, long limit){

        long now = System.currentTimeMillis() / 1000;
        long currentWindowStartTime = (now / windowSize)*(windowSize);
        long prevWindowStartTime = currentWindowStartTime - windowSize;

        String curKey = "rate_limit:" + prefix + ":" + identifier + ":" + currentWindowStartTime;
        String prevKey = "rate_limit:" + prefix + ":" + identifier + ":" + prevWindowStartTime;
        List<String> keys = Arrays.asList(curKey,prevKey);

        try {
            String result = redisTemplate.execute(redisScript, keys, String.valueOf(now), String.valueOf(windowSize), String.valueOf(limit));
            boolean allowed = "1".equals(result);

            // Decision log (IMPORTANT)
            log.info("Rate limit decision",
                    keyValue("event", "RATE_LIMIT_DECISION"),
                    keyValue("prefix", prefix),
                    keyValue("identifier", identifier),
                    keyValue("allowed", allowed),
                    keyValue("limit", limit),
                    keyValue("windowSize", windowSize)
            );

            return allowed;
        } catch (Exception ex) {
            // Redis failure
            log.error("Redis rate limit execution failed",
                    keyValue("event", "REDIS_FAILURE"),
                    keyValue("prefix", prefix),
                    keyValue("identifier", identifier),
                    keyValue("errorType", ex.getClass().getSimpleName()),
                    keyValue("error_message", ex.getMessage()),
                    ex
            );

            throw ex;

        }
    }

    private boolean checkFallback(String identifier, String prefix, long windowSize, long limit,Exception ex){
        log.error("Redis circuit breaker triggered - allowing request",
                keyValue("event", "REDIS_CB_FALLBACK"),
                keyValue("prefix", prefix),
                keyValue("identifier", identifier),
                keyValue("fallback", "ALLOW"),
                keyValue("errorType", ex.getClass().getSimpleName()),
                keyValue("error_message", ex.getMessage())
        );

        return true; // fail open
    }
}
