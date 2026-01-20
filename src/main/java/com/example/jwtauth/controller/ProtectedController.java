package com.example.jwtauth.controller;

import com.example.jwtauth.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/protected")
public class ProtectedController {

    @GetMapping("/data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProtectedData(
            @AuthenticationPrincipal UserDetails userDetails ){
        log.info("Protected data access requested by user: {}", userDetails.getUsername());

        Map<String, Object> protectedData = Map.of(
                "message", "This is protected data",
                "user", userDetails.getUsername(),
                "roles", userDetails.getAuthorities()
        );
        return ResponseEntity.ok(ApiResponse.success(protectedData, "Protected data retrieved successfully"));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails ){
        log.info("User profile access requested by user: {}", userDetails.getUsername());

        Map<String, Object> userProfile = Map.of(
                "username", userDetails.getUsername(),
                "authorities", userDetails.getAuthorities(),
                "enabled", userDetails.isEnabled(),
                "roles", userDetails.getAuthorities()
        );
        return ResponseEntity.ok(ApiResponse.success(userProfile, "User profile retrieved successfully"));
    }
}
