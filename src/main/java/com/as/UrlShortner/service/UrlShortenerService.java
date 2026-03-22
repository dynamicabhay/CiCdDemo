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

            UrlMappings newUrl = new UrlMappings(originalUrl, Instant.now(), key, nextId);
            UrlMappings entityWithId = urlMappingRepository.save(newUrl);

            // ✅ URL CREATED
            log.atInfo()
                    .addKeyValue("event", "URL_CREATED")
                    .addKeyValue("shortCode", key)
                    .log("Short URL created");

            return UrlShortnerResponse.builder()
                    .shortUrl(baseUrl + key)
                    .build();

        } catch (Exception ex) {

            // 🔴 DB FAILURE
            log.atError()
                    .addKeyValue("event", "URL_CREATION_FAILED")
                    .addKeyValue("originalUrl", request.getUrl())
                    .addKeyValue("errorType", ex.getClass().getSimpleName())
                    .addKeyValue("error_message", ex.getMessage())
                    .log("URL not stored in DB", ex);

            throw ex;
        }
    }

    @Transactional
    public String getUrl(String key) throws KeyNotFoundException {

        if (key == null || key.isBlank())
            throw new RuntimeException("URL not found");

        String redisKey = "URL:" + key;

        try {

            String result = redisTemplate.opsForValue().get(redisKey);

            // 🟢 Cache HIT
            if (result != null && !"NOT_FOUND".equalsIgnoreCase(result)) {

                log.atInfo()
                        .addKeyValue("event", "CACHE_HIT")
                        .addKeyValue("shortCode", key)
                        .log("Cache hit");

                return result;
            }

            // 🔴 Cached NOT_FOUND
            if ("NOT_FOUND".equalsIgnoreCase(result)) {

                log.atInfo()
                        .addKeyValue("event", "CACHE_NEGATIVE_HIT")
                        .addKeyValue("shortCode", key)
                        .log("Cache negative hit");

                return null;
            }

            // 🟡 Cache MISS
            log.atInfo()
                    .addKeyValue("event", "CACHE_MISS")
                    .addKeyValue("shortCode", key)
                    .log("Cache miss");

            // 🔄 Fetch from DB
            return urlMappingRepository.findByShortKey(key)
                    .map(url -> {

                        redisTemplate.opsForValue()
                                .set(redisKey, url.getOriginalUrl(), 24, TimeUnit.HOURS);

                        log.atInfo()
                                .addKeyValue("event", "DB_HIT")
                                .addKeyValue("shortCode", key)
                                .log("DB hit and cache updated");

                        return url.getOriginalUrl();
                    })
                    .orElseGet(() -> {

                        redisTemplate.opsForValue()
                                .set(redisKey, "NOT_FOUND", 1, TimeUnit.HOURS);

                        log.atWarn()
                                .addKeyValue("event", "URL_NOT_FOUND")
                                .addKeyValue("shortCode", key)
                                .log("URL not found");

                        return null;
                    });

        } catch (Exception ex) {

            // 🔴 Redis / DB failure
            log.atError()
                    .addKeyValue("event", "GET_URL_FAILURE")
                    .addKeyValue("shortCode", key)
                    .addKeyValue("errorType", ex.getClass().getSimpleName())
                    .addKeyValue("error_message", ex.getMessage())
                    .log("Get URL failed", ex);

            throw new RuntimeException(ex);
        }
    }


}
