package com.example.jwtauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType;
    private long expiresIn;
    private String userId;
    private boolean cached;  // Indiacates if the token was returned from cache
    private String message;

}
