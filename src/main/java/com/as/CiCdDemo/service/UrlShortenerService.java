package com.as.CiCdDemo.service;

import com.as.CiCdDemo.dto.UrlShortnerRequest;
import com.as.CiCdDemo.dto.UrlShortnerResponse;
import com.as.CiCdDemo.exceptions.AliasAlreadyTakenException;
import com.as.CiCdDemo.exceptions.KeyNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UrlShortenerService {

    public Base62 encoder;
    private Map<String,String> urlStore;
    private AtomicInteger counter;

    private String baseUrl;

    public UrlShortenerService(Base62 encoder,@Value("${server.domain}") String domain){

        this.encoder = encoder;
        urlStore = new HashMap<>();
        counter = new AtomicInteger(0);
        baseUrl = String.format("https://%s/", domain);
    }

    public UrlShortnerResponse process(UrlShortnerRequest request) throws AliasAlreadyTakenException {
        String key = null;
        if(request.getAlias() != null){
            key = request.getAlias().trim();
            if(urlStore.containsKey(key)) throw new AliasAlreadyTakenException(key);
        }
        else {
            long count = counter.incrementAndGet();
            key = encoder.encode(count);
        }
        urlStore.put(key, request.getUrl());
        String shortUrl = (baseUrl + key);
        return UrlShortnerResponse.builder().shortUrl(shortUrl).build();
    }

    public String getUrl(String key) throws KeyNotFoundException {
        if(urlStore.containsKey(key)) return urlStore.get(key);
        else throw new KeyNotFoundException(key);
    }
}
