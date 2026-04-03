package com.as.UrlShortner.security;

import com.as.UrlShortner.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Component
@Slf4j
public class RateLimiterFilter extends OncePerRequestFilter {
    private RateLimiterService rateLimiterService;



    public RateLimiterFilter(RateLimiterService rateLimiterService){
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String endpoint = request.getRequestURI();

        try {
            boolean allowed = rateLimiterService.isAllowed(request, response);

            if (!allowed) {
                // 🔴 RATE LIMIT BLOCK

                log.warn("event=RATE_LIMIT_BLOCK method={} endpoint={} status={}", method,endpoint,"NOT_ALLOWED");

                sendTooManyRequests(response);
                return;
            }

            // ✅ RATE LIMIT PASSED
            log.warn("event=RATE_LIMIT_PASS method={} endpoint={} status={}", method,endpoint,"ALLOWED");

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            // 🔴 Unexpected failure
            log.warn("event=RATE_LIMIT_ERROR method={} endpoint={} errorType={}", method,endpoint,ex.getClass().getSimpleName());

            throw ex;
        }
    }

    private void sendTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.getWriter().write("Too many requests. Please try again later.");
    }





}
