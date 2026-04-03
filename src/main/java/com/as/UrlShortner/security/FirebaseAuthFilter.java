package com.as.UrlShortner.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
public class FirebaseAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String endpoint = request.getRequestURI();

        try {
            // 🔵 REQUEST START
            log.info("event=REQUEST_START endpoint={} method={} - Request started", endpoint, method);

            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                try {
                    FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);

                    String uid = decodedToken.getUid();
                    String email = decodedToken.getEmail();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("event=AUTH_SUCCESS userId={} email={} - Authentication successful", uid, email);



                } catch (Exception e) {

                    log.warn("AUTH_FAILURE errorType={} error_message={}",
                            e.getClass().getSimpleName(),
                            e.getMessage());

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

            } else {
                // ⚠️ NO HEADER
                log.warn("AUTH_HEADER_MISSING: Missing or invalid Authorization header");
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {

            // 🔴 REQUEST FAILURE
            log.error("REQUEST_FAILED endpoint={} errorType={} error_message={}",
                    endpoint,
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    ex);

            throw ex;

        } finally {

            long duration = System.currentTimeMillis() - startTime;

            // 🔵 REQUEST COMPLETED
            log.info("REQUEST_COMPLETED endpoint={} method={} status={} duration={}",
                    endpoint,
                    method,
                    response.getStatus(),
                    duration);

            SecurityContextHolder.clearContext();
        }
    }
}