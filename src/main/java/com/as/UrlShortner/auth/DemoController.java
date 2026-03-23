package com.as.UrlShortner.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class DemoController {
    @GetMapping("/test")
    public ResponseEntity<Map<String,Object>> greet(Authentication authentication , HttpServletRequest request){
        Map<String,Object> details = new HashMap<>();
        if(authentication != null && authentication.isAuthenticated()){
            details.put("username",authentication.getName());
            details.put("roles",authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
            details.put("remoteIpAddress", request.getRemoteAddr());
            details.put("requestMethod", request.getMethod());
            details.put("requestUri", request.getRequestURI());
            details.put("userAgent", request.getHeader("User-Agent"));

        }

        return new ResponseEntity<>(details, HttpStatus.OK);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping(){
        return ResponseEntity.ok("PONG");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> admin(){
        return new ResponseEntity<>("hello from the DemoController-admin !!", HttpStatus.OK);
    }

    @GetMapping("/user")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> usr(){
        return new ResponseEntity<>("hello from the DemoController-user !!", HttpStatus.OK);
    }
}
