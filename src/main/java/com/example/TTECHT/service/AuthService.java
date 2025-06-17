package com.example.TTECHT.service;

import com.example.TTECHT.dto.repsonse.AuthenticatedResponse;
import com.example.TTECHT.dto.request.*;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthService {
//    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
    AuthenticatedResponse login(AuthenticatedRequest loginRequest);
    String register(RegisterUserRequest registerUserRequest);
    void logout(LogoutRequest request) throws ParseException,  JOSEException;
    AuthenticatedResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;
}
