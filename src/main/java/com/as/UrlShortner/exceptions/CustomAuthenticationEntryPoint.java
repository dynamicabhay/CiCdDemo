package com.as.UrlShortner.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Use a single ObjectMapper instance for performance
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Set the response status to 401 Unauthorized
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String message;
        // Check the cause of the exception to provide a more specific message
        Throwable cause = authException.getCause();
        Exception exception = (Exception) request.getAttribute("exception");
       // System.out.println(ex);
        if (exception != null) {
            if (exception instanceof SignatureException) {
                message = "Invalid JWT signature.";
            } else if (exception instanceof ExpiredJwtException) {
                message = "JWT has expired.";
            } else if (exception instanceof MalformedJwtException) {
                message = "Malformed JWT.";
            } else if (exception instanceof UnsupportedJwtException) {
                message = "Unsupported JWT.";
            } else {
                message = "Authentication failed due to an invalid token.";
            }
        } else {
            // This case handles when there's no token provided at all
            message = "Authentication Failed: An Authorization header is required.";
        }

        // Create the JSON response body
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", message);
        body.put("path", request.getRequestURI());

        // Write the JSON response to the output stream
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}