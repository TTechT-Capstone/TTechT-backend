package com.example.TTECHT.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.TTECHT.dto.repsonse.AuthenticatedResponse;
import com.example.TTECHT.dto.request.AuthenticatedRequest;
import com.example.TTECHT.dto.request.RegisterUserRequest;
import com.example.TTECHT.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticatedRequest loginRequest) {
        try {
            AuthenticatedResponse response = authService.login(loginRequest);
            System.out.println(response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserRequest registerRequest) {
        try {
            authService.register(registerRequest);
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Registration failed: " + e.getMessage());
        }
    }
    
    // @PostMapping("/logout")
    // public ResponseEntity<?> logout() {
    //     try {
    //         authService.logout();
    //         return ResponseEntity.ok("User logged out successfully");
    //     } catch (Exception e) {
    //         return ResponseEntity.status(500).body("Logout failed: " + e.getMessage());
    //     }
    // }

}
