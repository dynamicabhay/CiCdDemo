package com.as.UrlShortner.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class RateLimiterService {
    private RedisRateLimit redisRateLimit;

    @Value("${rate.limit.shorten.window.size}")
    private int rateLimitShortenWindowSize;
    @Value("${rate.limit.shorten.allowed.requests}")
    private int rateLimitShortenAllowedRequests;
    @Value("${rate.limit.redirection.window.size}")
    private int rateLimitRedirectionWindowSize;
    @Value("${rate.limit.redirection.window.allowed.requests}")
    private int rateLimitRedirectionAllowedRequests;

    public RateLimiterService(RedisRateLimit redisRateLimit){
        this.redisRateLimit = redisRateLimit;
    }

    public boolean isAllowed(HttpServletRequest request, HttpServletResponse response){
        String requestURI = request.getRequestURI();


        if (isRedirectionRequest(requestURI)) {

            String clientId = getClientIp(request);
            //System.out.println("clientId : " + clientId);
            if(!redisRateLimit.check(clientId,"redirect",rateLimitRedirectionWindowSize,rateLimitRedirectionAllowedRequests)){
                return false;
            }
        }
        else if(isShortenRequest(requestURI)){

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String identifier = auth.getPrincipal().toString();
            String prefix = "shorten";
            if(!redisRateLimit.check(identifier,prefix,rateLimitShortenWindowSize,rateLimitShortenAllowedRequests)) {
                return false;
            }
        }
        return true;

    }


    private boolean isShortenRequest(String uri){
        return uri.startsWith("/shorten");
    }

    private boolean isRedirectionRequest(String uri){
        return uri.startsWith(("/s/"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfwd = request.getHeader("X-Forwarded-For");
        if (xfwd != null && !xfwd.isEmpty()) {
            return xfwd.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }


}
