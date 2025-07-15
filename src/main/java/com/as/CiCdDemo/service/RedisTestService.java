package com.as.CiCdDemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTestService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public String testConnection(){
        redisTemplate.opsForValue().set("test-key","redis-connect");
        return redisTemplate.opsForValue().get("test-key");
    }



}
