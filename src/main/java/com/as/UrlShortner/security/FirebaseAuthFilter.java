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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Component
@Slf4j
public class FirebaseAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException,IOException {
        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String endpoint = request.getRequestURI();

        try {
            log.atInfo()
                    .addKeyValue("event", "REQUEST_START")
                    .addKeyValue("endpoint", endpoint)
                    .addKeyValue("method", method)
                    .log("Request started");


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

                    // AUTH SUCCESS
                    log.atInfo()
                            .addKeyValue("event", "AUTH_SUCCESS")
                            .addKeyValue("userId", uid)
                            .addKeyValue("email", email)
                            .log("Authentication successful");

                } catch (Exception e) {
                    //  AUTH FAILURE
                    log.atWarn()
                            .addKeyValue("event", "AUTH_FAILURE")
                            .addKeyValue("errorType", e.getClass().getSimpleName())
                            .addKeyValue("error_message", e.getMessage())
                            .log("Authentication failed");

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }else{
                //  NO TOKEN (optional depending on your design)
                log.atWarn()
                        .addKeyValue("event", "AUTH_HEADER_MISSING")
                        .log("Missing or invalid Authorization header");


            }

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            // 🔴 REQUEST FAILURE (catch anything unexpected)
            log.atError()
                    .addKeyValue("event", "REQUEST_FAILED")
                    .addKeyValue("endpoint", endpoint)
                    .addKeyValue("errorType", ex.getClass().getSimpleName())
                    .addKeyValue("error_message", ex.getMessage())
                    .log("Request Failed", ex);
            throw ex;
        }
        finally {
            long duration = System.currentTimeMillis() - startTime;

            //  EXIT LOG (always runs)
            log.atInfo()
                    .addKeyValue("event", "REQUEST_COMPLETED")
                    .addKeyValue("endpoint", endpoint)
                    .addKeyValue("method", method)
                    .addKeyValue("status", response.getStatus())
                    .addKeyValue("duration", duration)
                    .log("Request Completed");





            // Optional cleanup
            SecurityContextHolder.clearContext();
        }
    }
}
