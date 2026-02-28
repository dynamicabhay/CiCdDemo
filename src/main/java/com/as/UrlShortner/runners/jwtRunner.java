package com.as.UrlShortner.runners;

import com.as.UrlShortner.repository.UserRepository;
import com.as.UrlShortner.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

//@Component
@Slf4j
public class jwtRunner implements CommandLineRunner {

    @Autowired
    UserRepository repo;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserDetailsService userService;


    @Override
    public void run(String... args) throws Exception {
        UserDetails admin = userService.loadUserByUsername("abhaysinghjprk6@gmail.com");
        UserDetails user = userService.loadUserByUsername("chauhanruchita27@gmail.com");
        log.info("loaded user -> " + user);
        log.info("loaded user -> " + admin);
        String token = jwtService.generateToken(admin);
        log.info("jwt -> " + token);

        String subject = jwtService.extractUsername(token);
        log.info("subject -> " + subject);
        Claims claims = jwtService.extractAllClaims(token);
        log.info("claims -> " + claims);
        log.info("roles -> " + claims.get("Roles"));
    }
}
