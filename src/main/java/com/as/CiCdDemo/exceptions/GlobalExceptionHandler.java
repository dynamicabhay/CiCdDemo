package com.as.CiCdDemo.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Helper method to create a consistent error response structure
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredJwtException(ExpiredJwtException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "JWT has expired.", request.getRequestURI());
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJwtException(MalformedJwtException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid JWT token structure.", request.getRequestURI());
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<Map<String, Object>> handleSignatureException(SignatureException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid JWT signature.", request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "Access Denied. You do not have permission to access this resource.", request.getRequestURI());
    }

    // A catch-all for any other unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        // Log the exception for debugging purposes
        // ex.printStackTrace();
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request.getRequestURI());
    }

    @ExceptionHandler(AliasAlreadyTakenException.class)
    public ResponseEntity<Map<String,Object>> handleAliasAlreadyTakenException(AliasAlreadyTakenException ex, HttpServletRequest request){
        return createErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(KeyNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleAliasAlreadyTakenException(KeyNotFoundException ex, HttpServletRequest request){
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }


}
