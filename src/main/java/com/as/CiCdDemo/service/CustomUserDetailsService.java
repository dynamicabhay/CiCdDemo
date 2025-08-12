package com.as.CiCdDemo.service;

import com.as.CiCdDemo.dao.UserServiceDAO;
import com.as.CiCdDemo.dto.CustomUserDetails;
import com.as.CiCdDemo.dto.UserDTO;
import com.as.CiCdDemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    UserServiceDAO userService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new CustomUserDetails(userService.findUserByUsername(username));

    }
}
