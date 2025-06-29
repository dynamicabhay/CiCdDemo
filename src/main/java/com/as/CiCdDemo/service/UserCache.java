package com.as.CiCdDemo.service;

import com.as.CiCdDemo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCache {


    private final RedisTemplate<String,Object> redisTemplate;

    public void cacheUser(User user){
        String key = "user:"+user.getId();
        redisTemplate.opsForValue().set(key,user);
        System.out.println("user added to cache successfully: " + user);
    }

    public void cacheUserList(List<String> names){

        List<User> userList = new ArrayList<>();
        for(int i=0; i<names.size(); i++){
            userList.add(new User(i+6l,names.get(i),26+i,"jaipur"));
        }
        Map<String,Object> map = userList.stream().collect(Collectors.toMap(
             u -> "user:"+u.getId(), u -> u)
        );
        redisTemplate.opsForValue().multiSet(map);
        System.out.println("list of user added !!");
    }


}
