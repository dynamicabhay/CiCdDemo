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
            MDC.put("event", "REQUEST_START");
            MDC.put("endpoint", endpoint);
            MDC.put("method", method);

            log.info("Request started");

            MDC.clear();

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

                    // 🟢 AUTH SUCCESS
                    MDC.put("event", "AUTH_SUCCESS");
                    MDC.put("userId", uid);
                    MDC.put("email", email);

                    log.info("Authentication successful");

                    MDC.clear();

                } catch (Exception e) {

                    // 🔴 AUTH FAILURE
                    MDC.put("event", "AUTH_FAILURE");
                    MDC.put("errorType", e.getClass().getSimpleName());
                    MDC.put("error_message", e.getMessage());

                    log.warn("Authentication failed");

                    MDC.clear();

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

            } else {
                // ⚠️ NO HEADER
                MDC.put("event", "AUTH_HEADER_MISSING");

                log.warn("Missing or invalid Authorization header");

                MDC.clear();
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {

            // 🔴 REQUEST FAILURE
            MDC.put("event", "REQUEST_FAILED");
            MDC.put("endpoint", endpoint);
            MDC.put("errorType", ex.getClass().getSimpleName());
            MDC.put("error_message", ex.getMessage());

            log.error("Request Failed", ex);

            MDC.clear();

            throw ex;

        } finally {

            long duration = System.currentTimeMillis() - startTime;

            // 🔵 REQUEST COMPLETED
            MDC.put("event", "REQUEST_COMPLETED");
            MDC.put("endpoint", endpoint);
            MDC.put("method", method);
            MDC.put("status", String.valueOf(response.getStatus()));
            MDC.put("duration", String.valueOf(duration));

            log.info("Request completed");

            MDC.clear();

            SecurityContextHolder.clearContext();
        }
    }
}