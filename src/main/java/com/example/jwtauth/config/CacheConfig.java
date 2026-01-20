package com.example.jwtauth.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    public static final String TOKEN_CACHE = "tokenCache";

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationInMs;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(TOKEN_CACHE);
        cacheManager.setCaffeine(caffineCacheBuilder());
        return cacheManager;
    }

    private Caffeine<Object,Object> caffineCacheBuilder(){

        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(jwtExpirationInMs, java.util.concurrent.TimeUnit.MILLISECONDS)
                .recordStats();
    }
}
