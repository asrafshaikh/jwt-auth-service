package com.example.jwtauth.service;

import com.example.jwtauth.dto.LoginRequest;
import com.example.jwtauth.dto.LoginResponse;
import com.example.jwtauth.model.CachedTokenData;
import com.example.jwtauth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final UserDetailsServiceImpl userDetailsService;

    private final TokenCacheService tokenCacheService;

    private final JwtTokenProvider jwtTokenProvider;


/*    public AuthenticationService(AuthenticationManager authenticationManager, UserDetailsServiceImpl userDetailsService, TokenCacheService tokenCacheService, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.tokenCacheService = tokenCacheService;
        this.jwtTokenProvider = jwtTokenProvider;
    }*/


    public LoginResponse Login(LoginRequest loginRequest){
        log.info("Authenticating user: {}", loginRequest.getUserId());

        String userId = loginRequest.getUserId();

        if(tokenCacheService.hasValidCacheToken(userId)){
            log.info("Valid cached token found for user: {}.", userId);
            var userDetails = userDetailsService.loadUserByUsername(userId);
            var cachedTokenData = tokenCacheService.getOrCreateToken(userDetails);
            return  LoginResponse.builder()
                    .token(cachedTokenData.getToken())
                    .tokenType("Bearer")
                    .userId(userId)
                    .expiresIn(cachedTokenData.getRemainingTimeInSeconds())
                    .cached(true)
                    .message("Existing Token return from cache")
                    .build();
        }

        // Step 2: No cached token - validate credentials
        try{
            authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(
                    loginRequest.getUserId(),
                    loginRequest.getPassword()
            ));

            log.info("Credential validted for user:{}", loginRequest.getUserId());
        }
        catch (BadCredentialsException ex){
            log.warn("Invalid credentials for user: {}", loginRequest.getUserId());
            throw new BadCredentialsException("Invalid userId or password");
        }

       // Step 3: Load user details and generate new token

        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
        CachedTokenData cachedToken = tokenCacheService.getOrCreateToken(userDetails);

        return  LoginResponse.builder()
                .token(cachedToken.getToken())
                .tokenType("Bearer")
                .userId(userId)
                .expiresIn(cachedToken.getRemainingTimeInSeconds())
                .cached(false)
                .message("New token generated")
                .build();
    }

    /*
     logout user by invalidating their token in the cache
     */

    public void logout(String userId){
        log.info("Logging out user: {}", userId);
        tokenCacheService.invalidateToken(userId);
    }

    /*
    * Force generate a new token for the user by invalidating any existing token
    * */
    public LoginResponse refreshToken(String userId){
        log.info("Refreshing token for user: {}", userId);
        // Invalidate existing token
        tokenCacheService.invalidateToken(userId);

        // Load user details and generate new token
        var userDetails = userDetailsService.loadUserByUsername(userId);
        var cachedTokenData = tokenCacheService.getOrCreateToken(userDetails);
        log.info("Token refreshed for user: {}", userId);

        return  LoginResponse.builder()
                .token(cachedTokenData.getToken())
                .tokenType("Bearer")
                .userId(userId)
                .expiresIn(cachedTokenData.getRemainingTimeInSeconds())
                .cached(false)
                .message("New token generated (forced refresh)")
                .build();
    }
}
