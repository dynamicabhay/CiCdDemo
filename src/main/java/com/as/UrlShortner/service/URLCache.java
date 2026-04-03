package com.as.UrlShortner.service;

import com.as.UrlShortner.exceptions.KeyNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@RequiredArgsConstructor
@Service
@Slf4j
public class URLCache {
    private final StringRedisTemplate redisTemplate;

    public void putUrl(String key, String value, int timeout, TimeUnit unit){
        try{
            redisTemplate.opsForValue().set(key,value,timeout,unit);
        } catch (RuntimeException ex) {
            log.warn("Redis is facing some problems, " , ex);
        }

    }

    public String getUrl(String key){
        try{
            String url = redisTemplate.opsForValue().get(key);
           // log.info("URL ==== " + url);
            // cache hit
            if(url != null && !url.isBlank() && !"NOT_FOUND".equalsIgnoreCase(url)) {
                log.info("event=CACHE_HIT shortCode={}", key);
                return url;
            }

            // negative cache
            if("NOT_FOUND".equalsIgnoreCase(url)){
                log.info("Negative Cache Hit shortCode={}", key);
                return url;
            }

            log.info("event=CACHE_MISS shortCode={}",key);

        } catch (RuntimeException ex) {
            log.warn("Redis is facing some problems, " , ex);
        }
        return null;
    }
}
