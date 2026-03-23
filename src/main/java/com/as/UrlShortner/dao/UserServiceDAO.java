package com.as.UrlShortner.dao;

import com.as.UrlShortner.dto.UserDTO;
import com.as.UrlShortner.model.User;
import com.as.UrlShortner.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class UserServiceDAO {

    private final UserRepository userRepository;

    public UserServiceDAO(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // This method is cached. It returns the simple User entity.
    @Cacheable(value = "userCache", key = "#username")
    public UserDTO findUserByUsername(String username) {
        System.out.println("--- Caching Service: Hitting database to get user: " + username + " ---");
        User entityUser = userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("username: " + username + " not exists in db !!")
        );
        return UserDTO.builder().username(entityUser.getUsername()).password(entityUser.getPassword())
                .roles(new HashSet<>(entityUser.getRoles()))
                .build();

    }

}
