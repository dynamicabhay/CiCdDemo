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

    @CircuitBreaker(name="redis", fallbackMethod = "checkFallback")
    public boolean check(String identifier, String prefix, long windowSize, long limit){
        StringBuilder currentKeySb = new StringBuilder();
        StringBuilder previousKeySb = new StringBuilder();
        long now = System.currentTimeMillis() / 1000;
        long currentWindowStartTime = (now / windowSize)*(windowSize);
        long prevWindowStartTime = currentWindowStartTime - windowSize;

        String curKey = currentKeySb.append("rate_limit").append(":").append(prefix)
                .append(":").append(identifier).append(":").append(currentWindowStartTime).toString();
        String prevKey = previousKeySb.append("rate_limit").append(":").append(prefix)
                .append(":").append(identifier).append(":").append(prevWindowStartTime).toString();

       /* System.out.println("currentKey: " + curKey);
        System.out.println("prevKey: " + prevKey);*/
        List<String> keys = Arrays.asList(curKey,prevKey);
        //System.out.println("Redis Script Output: " + redisScript.getResultType());
        String result = redisTemplate.execute(redisScript,keys,String.valueOf(now),String.valueOf(windowSize),String.valueOf(limit));
        //System.out.println("script result: " + result);
        return "1".equals(result);
    }

    private boolean checkFallback(String identifier, String prefix, long windowSize, long limit,Exception ex){
        log.error("Redis circuit breaker triggered for identifier: {}, prefix: {}. Allowing request as fallback.", identifier, prefix);
        return true;
    }
}
