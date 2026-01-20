package com.example.jwtauth.service;

import com.example.jwtauth.config.CacheConfig;
import com.example.jwtauth.model.CachedTokenData;
import com.example.jwtauth.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class TokenCacheService {

    private static final Logger log = LoggerFactory.getLogger(TokenCacheService.class);

    private final CacheManager cacheManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-buffer:300000}")
    private long refreshBuffer;

    public TokenCacheService(CacheManager cacheManager, JwtTokenProvider jwtTokenProvider) {
        this.cacheManager = cacheManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public CachedTokenData getOrCreateToken(UserDetails userDetails){
        log.info("Getting or creating token for user: {}", userDetails.getUsername());
        String userId = userDetails.getUsername();
        Optional<CachedTokenData> cachedToken = getFromCache(userId);

        if(cachedToken.isPresent()){
            CachedTokenData token = cachedToken.get();
            log.info("Found token in cache for user: {}, expired:{}, aboutToExpire:{}", userDetails.getUsername(),token.isExpired()
                    ,token.isAboutToExpire(refreshBuffer));

            if(!token.isExpired() && !token.isAboutToExpire(refreshBuffer)){
                log.info("Returning cached token for user: {}", userDetails.getUsername());
                return token;
            }
            else {
                log.info("Cached token nearing expiration for user: {}. Generating new token.", userDetails.getUsername());
                removeFromCache(userId);
            }
        } else {
            log.info("No cached token found for user: {}. Generating new token.", userDetails.getUsername());
        }
        log.info("Generating new token for user: {}", userDetails.getUsername());
        return generateAndCacheToken(userDetails);
    }

    private CachedTokenData generateAndCacheToken(UserDetails userDetails){
        String token = jwtTokenProvider.generateToken(userDetails);
        Instant expiresAt = jwtTokenProvider.getExpirationInstant(token);
        CachedTokenData cachedToken = CachedTokenData.builder()
                .token(token)
                .userId(userDetails.getUsername())
                .createdAt(Instant.now())
                .expiresAt(expiresAt)
                .build();
        putInCache(userDetails.getUsername(), cachedToken);
        log.info("Generated and cached new token for user: {}, expiresAt{}", userDetails.getUsername(),expiresAt);
        return cachedToken;
    }

    private Optional<CachedTokenData> getFromCache(String userId){
        log.info("Retrieving token from cache for userId: {}", userId);
        Cache cache = cacheManager.getCache("tokenCache");
        if(cache != null) {
            Cache.ValueWrapper wrapper = cache.get(userId);
            log.warn("Cache lookup for user {} : wrapper={}", userId, wrapper);

            if(wrapper !=null && wrapper.get() != null) {
               CachedTokenData data = (CachedTokenData) wrapper.get();
               log.info("Retrieved from cache for user{}: token data{}",userId,data.getToken());
               return Optional.of(data);
            }
        }
     return Optional.empty();
    }

    private void putInCache(String userId,CachedTokenData cachedToken){
        log.info("Storing token in cache for userId: {}", cachedToken.getUserId());
        Cache cache = cacheManager.getCache(CacheConfig.TOKEN_CACHE);
        if(cache != null) {
            cache.put(userId, cachedToken);
            log.info("Token cached for userId: {}", cachedToken.getUserId());
        } else {
            log.error("Cache is null. Unable to store token for userId: {}", userId);
        }
    }

    public void removeFromCache(String userId){
        log.info("Removing token from cache for userId: {}", userId);
        Cache cache = cacheManager.getCache(CacheConfig.TOKEN_CACHE);
        if(cache != null) {
            cache.evict(userId);
            log.info("Token evicted from cache for userId: {}", userId);
        } else {
            log.error("Cache is null. Unable to evict token for userId: {}", userId);
        }
    }

    public void invalidateToken(String userId){

        removeFromCache(userId);
        log.info("Token invalidated for user: {}", userId);
    }

    public boolean hasValidCacheToken(String userId){
        log.info("Checking cache for valid token for userId: {}", userId);
        Optional<CachedTokenData> cachedToken = getFromCache(userId);
        boolean hasVlaid = cachedToken.isPresent() &&
                !cachedToken.get().isExpired();
        log.info("hasValidCacheToken for userId {}: {}", userId, hasVlaid);
        return hasVlaid;
    }
}
