package com.as.UrlShortner.service;

import com.as.UrlShortner.dto.UrlShortnerRequest;
import com.as.UrlShortner.dto.UrlShortnerResponse;
import com.as.UrlShortner.exceptions.KeyNotFoundException;
import com.as.UrlShortner.model.UrlMappings;
import com.as.UrlShortner.repository.UrlMappingRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Service
@Slf4j
public class UrlShortenerService {

    public Base62 encoder;
    private UrlMappingRepository urlMappingRepository;
    private String baseUrl;
    EntityManager entityManager;
    private StringRedisTemplate redisTemplate;
    private final IdGeneratorService idGeneratorService;

    public UrlShortenerService(Base62 encoder,@Value("${server.domain}") String domain,UrlMappingRepository urlMappingRepository,EntityManager entityManager,StringRedisTemplate redisTemplate,IdGeneratorService idGeneratorService){

        this.encoder = encoder;
        this.urlMappingRepository = urlMappingRepository;
        baseUrl = String.format("https://%s/s/", domain);
        this.entityManager = entityManager;
        this.redisTemplate = redisTemplate;
        this.idGeneratorService = idGeneratorService;
    }

    @Transactional
    public UrlShortnerResponse process(UrlShortnerRequest request) {
        try {

            long nextId = idGeneratorService.getNextid();
            String originalUrl = request.getUrl().trim();
            String key = encoder.encode(nextId);
            UrlMappings newUrl = new UrlMappings(originalUrl,Instant.now(),key,nextId);

            UrlMappings entityWithId = urlMappingRepository.save(newUrl);

            log.info("Short URL created",
                    keyValue("event", "URL_CREATED"),
                    keyValue("shortCode", key)
            );

            return UrlShortnerResponse.builder()
                    .shortUrl(baseUrl + key)
                    .build();


        } catch (Exception ex) {

            log.error("{} is not stored in DB", request.getUrl());
            throw ex;
        }
    }

    @Transactional
    public String getUrl(String key) throws KeyNotFoundException {
        if(key == null || key.isBlank()) throw new RuntimeException("URL not found ");
        String redisKey = "URL:" + key;

        try {

            String result = redisTemplate.opsForValue().get(redisKey);

            // 🟢 Cache HIT
            if (result != null) {

                log.info("Cache hit",
                        keyValue("event", "CACHE_HIT"),
                        keyValue("shortCode", key)
                );

                return result;
            }

            // 🔴 Cached NOT_FOUND
            if ("NOT_FOUND".equalsIgnoreCase(result)) {

                log.info("Cache negative hit",
                        keyValue("event", "CACHE_NEGATIVE_HIT"),
                        keyValue("shortCode", key)
                );

                return null;
            }

            // 🟡 Cache MISS
            log.info("Cache miss",
                    keyValue("event", "CACHE_MISS"),
                    keyValue("shortCode", key)
            );

            // 🔄 Fetch from DB
            return urlMappingRepository.findByShortKey(key)
                    .map(url -> {

                        redisTemplate.opsForValue()
                                .set(redisKey, url.getOriginalUrl(), 24, TimeUnit.HOURS);

                        log.info("DB hit and cache updated",
                                keyValue("event", "DB_HIT"),
                                keyValue("shortCode", key)
                        );

                        return url.getOriginalUrl();
                    })
                    .orElseGet(() -> {

                        redisTemplate.opsForValue()
                                .set(redisKey, "NOT_FOUND", 1, TimeUnit.HOURS);

                        log.warn("URL not found",
                                keyValue("event", "URL_NOT_FOUND"),
                                keyValue("shortCode", key)
                        );

                        return null;
                    });
        } catch (Exception ex) {

            // 🔴 Redis / DB failure
            log.error("Get URL failed",
                    keyValue("event", "GET_URL_FAILURE"),
                    keyValue("shortCode", key),
                    keyValue("errorType", ex.getClass().getSimpleName()),
                    keyValue("message", ex.getMessage()),
                    ex
            );
            throw new RuntimeException(ex);
        }

    }


}
