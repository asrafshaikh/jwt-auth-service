package com.example.jwtauth.controller;

import com.example.jwtauth.dto.ApiResponse;
import com.example.jwtauth.dto.LoginRequest;
import com.example.jwtauth.dto.LoginResponse;
import com.example.jwtauth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest){
        log.info("Login request received for user: {}", loginRequest.getUserId());
        LoginResponse loginResponse = authenticationService.Login(loginRequest);
        String message = loginResponse.isCached()
                ? "Login successful - returning cached token"
                : "Login successful - new token generated";
        return ResponseEntity.ok(ApiResponse.success(loginResponse, message));

    }


    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        // Implementation for logout can be added here
        if(userDetails!=null){
            authenticationService.logout(userDetails.getUsername());
            return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
        }
        return ResponseEntity.ok(ApiResponse.success(null, "No Active session"));
    }

    @PostMapping("/refresh-token")
    public  ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @AuthenticationPrincipal UserDetails userDetails){
        log.info("Token refresh request received for user: {}", userDetails.getUsername());

        if(userDetails==null){

            return ResponseEntity.badRequest().body(ApiResponse.error("No authenticated user"));
        }
        LoginResponse loginResponse = authenticationService.refreshToken(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Token refreshed successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck(){
        log.info("Health check endpoint called");
        return ResponseEntity.ok(ApiResponse.success("ok", "Service is up and running"));
    }
}
