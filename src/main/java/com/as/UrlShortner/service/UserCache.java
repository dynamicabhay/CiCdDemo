package com.as.UrlShortner.service;

import com.as.UrlShortner.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

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
        /*
        List<UserDTO> userList = new ArrayList<>();
        for(int i=0; i<names.size(); i++){
            userList.add(new UserDTO(i+6,names.get(i)));
        }
        Map<String,Object> map = userList.stream().collect(Collectors.toMap(
             u -> "user:"+u.getId(), u -> u)
        );
        redisTemplate.opsForValue().multiSet(map);
        System.out.println("list of user added !!");

         */
    }


}
