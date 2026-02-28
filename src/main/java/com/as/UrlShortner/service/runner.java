package com.as.UrlShortner.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

//@Component
public class runner implements CommandLineRunner {

    @Autowired
    UserCache userCacheService;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Autowired
    Base62 encoder;
    @Override
    public void run(String... args) throws Exception {

        // creating dummy user
        // creating list of users
       // List<String> list =  List.of("hemendra","ashish","rahul","anurag","ruchie");


        //userCacheService.cacheUserList(list);
        String res = encoder.encode(106789);
        System.out.println("encoding " + res);

    }

    @PostConstruct
    public void demo(){
        System.out.println("######################### " + redisHost + " #################################");
    }

}
