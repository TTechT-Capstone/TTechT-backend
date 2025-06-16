package com.example.TTECHT.service;

import com.example.TTECHT.dto.repsonse.AuthenticatedResponse;
import com.example.TTECHT.dto.request.AuthenticatedRequest;
import com.example.TTECHT.dto.request.RegisterUserRequest;

public interface AuthService {
    AuthenticatedResponse login(AuthenticatedRequest loginRequest);
    String register(RegisterUserRequest registerUserRequest);
}
