package com.example.jwtauth.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserDetailsServiceImpl  implements UserDetailsService {

    private final Map<String, UserDetails> users = new HashMap<>();

    @PostConstruct
    public void init(){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        users.put("john", User.builder()
                .username("john")
                .password(encoder.encode("password123"))
                .roles("USER")
                .build());

        users.put("asraf", User.builder()
                .username("asraf")
                .password(encoder.encode("mypassword"))
                .roles("USER")
                .build());

        users.put("admin", User.builder()
                .username("admin")
                .password(encoder.encode("adminpass"))
                .roles("ADMIN","USER")
                .build());

        log.info("Initialized in-memory user store with users: {}", users.size());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user =users.get(username);
        if (user == null) {
           log.warn("User not found: {}", username);
           throw  new UsernameNotFoundException("User not found: " + username);
        }
        log.debug("User found: {}", username);
        return user;
    }
}
