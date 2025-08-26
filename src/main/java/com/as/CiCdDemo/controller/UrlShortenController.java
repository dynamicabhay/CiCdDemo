package com.as.CiCdDemo.controller;

import com.as.CiCdDemo.dto.UrlShortnerRequest;
import com.as.CiCdDemo.dto.UrlShortnerResponse;
import com.as.CiCdDemo.service.RateLimiterNonAuthenticatedService;
import com.as.CiCdDemo.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
//@RequestMapping("/tiny-url")
public class UrlShortenController {

    @Autowired
    UrlShortenerService urlShortenerService;

    @Autowired
    RateLimiterNonAuthenticatedService rs;

    @PostMapping("/shorten")
    public ResponseEntity<UrlShortnerResponse> shortenUrl(@RequestBody UrlShortnerRequest request){
       // System.out.println("hello");
        UrlShortnerResponse response = urlShortenerService.process(request);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }

    @GetMapping("/s/{shortKey}")
    public ResponseEntity<Void> redirectToLongUrl(@PathVariable String shortKey) {
        // Look up the long URL from our "database"
        if(shortKey == null) return ResponseEntity.notFound().build();

        String longUrl = urlShortenerService.getUrl(shortKey.trim());

        if (longUrl != null) {

            // If found, issue an HTTP 301 Permanent Redirect
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(longUrl))
                    .build();
        } else {
            // If not found, return an HTTP 404 Not Found response
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/flush")
    public String flush(){
        rs.flush();
        return "done";
    }

}
