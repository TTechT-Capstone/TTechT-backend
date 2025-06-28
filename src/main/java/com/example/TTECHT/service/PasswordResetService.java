package com.example.TTECHT.service;

public interface PasswordResetService {

    void initiateForgotPassword(String email);
    void resetPassword(String token, String newPassword);
    boolean validateToken(String token);
}
