package com.as.CiCdDemo.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class runner implements CommandLineRunner {

    @Autowired
    UserCache userCacheService;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Override
    public void run(String... args) throws Exception {

        // creating dummy user
        // creating list of users
        List<String> list =  List.of("hemendra","ashish","rahul","anurag","ruchie");


        userCacheService.cacheUserList(list);

    }

    @PostConstruct
    public void demo(){
        System.out.println("######################### " + redisHost + " #################################");
    }

}
