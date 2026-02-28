package com.as.UrlShortner.interceptors;

import com.as.UrlShortner.service.RateLimiterNonAuthenticatedService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimiterInterceptor implements HandlerInterceptor {

    @Autowired
    RateLimiterNonAuthenticatedService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(!(handler instanceof HandlerMethod)) return true;

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth instanceof AnonymousAuthenticationToken){

            String key = request.getRemoteAddr();

           // System.out.println("user is not authenticated !! " + key);
            if(!rateLimiterService.isAllowed(key)){
                response.setStatus(429);
                response.getWriter().write("too many requests");
                return false;
            }

        }

        return true;


    }
}
