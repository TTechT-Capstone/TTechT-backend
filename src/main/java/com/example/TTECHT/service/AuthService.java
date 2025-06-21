package com.example.TTECHT.service;

import com.example.TTECHT.dto.repsonse.AuthenticationResponse;
import com.example.TTECHT.dto.repsonse.IntrospectResponse;
import com.example.TTECHT.dto.request.*;
import com.nimbusds.jose.JOSEException;
import java.text.ParseException;

public interface AuthService {
    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
    AuthenticationResponse authenticate(AuthenticationRequest request);
    void logout(LogoutRequest request) throws ParseException, JOSEException;
    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;
}
