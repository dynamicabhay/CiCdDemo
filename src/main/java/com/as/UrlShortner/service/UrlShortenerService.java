package com.as.UrlShortner.service;

import com.as.UrlShortner.dto.UrlShortnerRequest;
import com.as.UrlShortner.dto.UrlShortnerResponse;
import com.as.UrlShortner.exceptions.KeyNotFoundException;
import com.as.UrlShortner.model.UrlMappings;
import com.as.UrlShortner.repository.UrlMappingRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
public class UrlShortenerService {

    public Base62 encoder;
    private UrlMappingRepository urlMappingRepository;
    private String baseUrl;
    EntityManager entityManager;

    public UrlShortenerService(Base62 encoder,@Value("${server.domain}") String domain,UrlMappingRepository urlMappingRepository,EntityManager entityManager){

        this.encoder = encoder;
        this.urlMappingRepository = urlMappingRepository;
        baseUrl = String.format("https://%s/s/", domain);
        this.entityManager = entityManager;
    }

    @Transactional
    public UrlShortnerResponse process(UrlShortnerRequest request) {
        try {
            Long nextId = ((Number) entityManager
                    .createNativeQuery("SELECT nextval('url_sequence')")
                    .getSingleResult()).longValue();

            String originalUrl = request.getUrl().trim();
            String key = encoder.encode(nextId);
            UrlMappings newUrl = new UrlMappings(originalUrl,Instant.now(),key,nextId);

            UrlMappings entityWithId = urlMappingRepository.save(newUrl);
            String shortUrl = (baseUrl + key);
            return UrlShortnerResponse.builder().shortUrl(shortUrl).build();
        } catch (Exception e) {

            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public String getUrl(String key) throws KeyNotFoundException {
        if(key == null || key.isBlank()) throw new RuntimeException("URL not found ");

            UrlMappings mapping = urlMappingRepository.findByShortKey(key.trim()).orElseThrow(() -> new RuntimeException());
            mapping.setVisitCount(mapping.getVisitCount()+1);

            System.out.println("shortKey: " + key);

            return mapping.getOriginalUrl();

    }


}
