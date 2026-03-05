package com.as.UrlShortner.security;

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
    private StringRedisTemplate redisTemplate;
    private DefaultRedisScript<String> redisScript;

    @Value("${rate.limit.shorten.window.size}")
    private int rateLimitShortenWindowSize;
    @Value("${rate.limit.shorten.allowed.requests}")
    private int rateLimitShortenAllowedRequests;
    @Value("${rate.limit.redirection.window.size}")
    private int rateLimitRedirectionWindowSize;
    @Value("${rate.limit.redirection.window.allowed.requests}")
    private int rateLimitRedirectionAllowedRequests;

    public RateLimiterFilter(StringRedisTemplate redisTemplate, DefaultRedisScript<String> redisScript){
        this.redisScript = redisScript;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        if (isRedirectionRequest(requestURI)) {

            String clientId = getClientIp(request);
            System.out.println("clientId : " + clientId);
            if(!isRequestAllowed(clientId,"redirect",rateLimitRedirectionWindowSize,rateLimitRedirectionAllowedRequests)){
                sendTooManyRequests(response);
            }
        }
        else if(isShortenRequest(requestURI)){

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                // Should not happen if you've secured the endpoint, but handle gracefully
                response.sendError(HttpStatus.UNAUTHORIZED.value());
                return;
            }
            String identifier = auth.getPrincipal().toString();
            String prefix = "shorten";
            if(!isRequestAllowed(identifier,prefix,rateLimitShortenWindowSize,rateLimitShortenAllowedRequests)) {
                sendTooManyRequests(response);
                return;
            }
        }
        filterChain.doFilter(request,response);
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

    private void sendTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.getWriter().write("Too many requests. Please try again later.");
    }

    private boolean isRequestAllowed(String identifier, String prefix, long windowSize, long limit){
        StringBuilder currentKeySb = new StringBuilder();
        StringBuilder previousKeySb = new StringBuilder();
        long now = System.currentTimeMillis() / 1000;
        long currentWindowStartTime = (now / windowSize)*(windowSize);
        long prevWindowStartTime = currentWindowStartTime - windowSize;

        String curKey = currentKeySb.append("rate_limit").append(":").append(prefix)
                .append(":").append(identifier).append(":").append(currentWindowStartTime).toString();
        String prevKey = previousKeySb.append("rate_limit").append(":").append(prefix)
                .append(":").append(identifier).append(":").append(prevWindowStartTime).toString();

       /* System.out.println("currentKey: " + curKey);
        System.out.println("prevKey: " + prevKey);*/
        List<String> keys = Arrays.asList(curKey,prevKey);
        //System.out.println("Redis Script Output: " + redisScript.getResultType());
        String result = redisTemplate.execute(redisScript,keys,String.valueOf(now),String.valueOf(windowSize),String.valueOf(limit));
        //System.out.println("script result: " + result);
        return "1".equals(result);
    }




}
