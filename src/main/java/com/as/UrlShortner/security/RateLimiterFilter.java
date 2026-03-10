package com.as.UrlShortner.security;

import com.as.UrlShortner.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class RateLimiterFilter extends OncePerRequestFilter {
    private RateLimiterService rateLimiterService;



    public RateLimiterFilter(RateLimiterService rateLimiterService){
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(!rateLimiterService.isAllowed(request,response)){
            sendTooManyRequests(response);
        }

        filterChain.doFilter(request,response);
    }


    private void sendTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.getWriter().write("Too many requests. Please try again later.");
    }





}
