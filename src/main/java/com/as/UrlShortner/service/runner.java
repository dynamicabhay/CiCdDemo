package com.as.UrlShortner.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class runner implements CommandLineRunner {



    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Autowired
    Base62 encoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void run(String... args) throws Exception {

        redisTemplate.opsForValue().set("Ping","Pong");
        String result = redisTemplate.opsForValue().get("test-key");
        System.out.println("================== Redis is connected: ==============" + result);

    }

    @PostConstruct
    public void demo(){
        System.out.println("######################### " + redisHost + " #################################");
    }

}
