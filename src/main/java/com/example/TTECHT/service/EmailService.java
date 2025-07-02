package com.example.TTECHT.service;

public interface EmailService {
    void sendPasswordResetEmail(String email, String token, String firstName);
    void sendPasswordResetConfirmationEmail(String email, String firstName);
}
