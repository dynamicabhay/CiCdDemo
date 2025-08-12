package com.as.CiCdDemo.service;

import com.as.CiCdDemo.dto.UrlShortnerResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UrlShortenerService {

    public Base62 encoder;
    private Map<Long,String> urlStore;
    private AtomicInteger counter;
    private String baseUrl = "http://localhost:8080/tiny-url/";

    public UrlShortenerService(Base62 encoder){
        this.encoder = encoder;
        urlStore = new HashMap<>();
        counter = new AtomicInteger(0);
    }

    public UrlShortnerResponse process(String url){

        long count = counter.incrementAndGet();
        String key = encoder.encode(count);
        urlStore.put(count,url);
        String shortUrl = (baseUrl + key);
        return UrlShortnerResponse.builder().shortUrl(shortUrl).build();
    }

    public String getUrl(String key){
        long id = encoder.decode(key);
        return urlStore.get(id);
    }
}
