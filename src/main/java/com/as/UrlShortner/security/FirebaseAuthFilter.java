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
            log.info("Request started",
                    keyValue("event", "REQUEST_START"),
                    keyValue("endpoint", endpoint),
                    keyValue("method", method)
            );

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
                    log.info("Authentication successful",
                            keyValue("event", "AUTH_SUCCESS"),
                            keyValue("userId", uid),
                            keyValue("email", email)
                    );
                } catch (Exception e) {
                    //  AUTH FAILURE
                    log.warn("Authentication failed",
                            keyValue("event", "AUTH_FAILURE"),
                            keyValue("errorType", e.getClass().getSimpleName()),
                            keyValue("error_message", e.getMessage())
                    );

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }else{
                //  NO TOKEN (optional depending on your design)
                log.warn("Missing or invalid Authorization header",
                        keyValue("event", "AUTH_HEADER_MISSING")
                );
            }

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            // 🔴 REQUEST FAILURE (catch anything unexpected)
            log.error("Request failed",
                    keyValue("event", "REQUEST_FAILED"),
                    keyValue("endpoint", endpoint),
                    keyValue("errorType", ex.getClass().getSimpleName()),
                    keyValue("error_message", ex.getMessage()),
                    ex
            );

            throw ex;
        }
        finally {
            long duration = System.currentTimeMillis() - startTime;

            //  EXIT LOG (always runs)
            log.info("Request completed",
                    keyValue("event", "REQUEST_COMPLETED"),
                    keyValue("endpoint", endpoint),
                    keyValue("method", method),
                    keyValue("status", response.getStatus()),
                    keyValue("duration", duration)
            );

            // Optional cleanup
            SecurityContextHolder.clearContext();
        }
    }
}
