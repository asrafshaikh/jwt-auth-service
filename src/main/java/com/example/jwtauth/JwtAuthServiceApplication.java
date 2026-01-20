package com.example.jwtauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class JwtAuthServiceApplication {

    public static void main(String[] args) {
        System.out.println("JWT Auth Service Application Started");
        SpringApplication.run(JwtAuthServiceApplication.class, args);
    }
}
