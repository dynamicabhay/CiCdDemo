package com.as.CiCdDemo.runners;

import com.as.CiCdDemo.dto.CustomUserDetails;
import com.as.CiCdDemo.model.Role;
import com.as.CiCdDemo.model.User;
import com.as.CiCdDemo.repository.UserRepository;
import com.as.CiCdDemo.service.CustomUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//@Component
@Slf4j
public class AddUser implements CommandLineRunner {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    CustomUserDetailsService uds;
    @Override
    public void run(String... args) throws Exception {
        log.info("inside Adduser Runner !!");
        /*
        if(userRepository.count() == 0) {
            User adminUser = User.builder().username("abhaysinghjprk6@gmail.com")
                    .password(encoder.encode("admin"))
                    .roles(new HashSet<>(List.of(Role.ROLE_ADMIN)))
                    .build();

            userRepository.save(adminUser);
            log.info("admin user succesfully added to db !!");

         */

        User normal = User.builder().username("chauhanruchita27@gmail.com")
                .password(encoder.encode("ruchita"))
                .roles(new HashSet<>(List.of(Role.ROLE_USER)))
                .build();
        userRepository.save(normal);

       /* else{
            log.info("users already present in the db !!");
            UserDetails user = uds.loadUserByUsername("abhaysinghjprk6@gmail.com");
            System.out.println(user);
        }*/


    }
}
