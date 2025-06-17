package com.example.TTECHT.controller;

import com.example.TTECHT.dto.request.LogoutRequest;
import com.example.TTECHT.dto.request.RefreshRequest;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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

import javax.security.auth.login.AccountLockedException;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticatedResponse> login(@RequestBody AuthenticatedRequest loginRequest) {
        try {
            AuthenticatedResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthenticatedResponse("Login failed: " + e.getMessage(), false, null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticatedResponse> register(@RequestBody RegisterUserRequest registerRequest) {
        try {
            authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthenticatedResponse("User registered successfully", true, null));
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthenticatedResponse("Registration failed: " + e.getMessage(), false, null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequest logoutRequest) {
        try {
            authService.logout(logoutRequest);
            return ResponseEntity.ok("User logged out successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Logout failed: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticatedResponse> refreshToken(@RequestBody RefreshRequest refreshRequest) {
        try {
            AuthenticatedResponse response = authService.refreshToken(refreshRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthenticatedResponse("Token refresh failed: " + e.getMessage(), false, null));
        }
    }
}
