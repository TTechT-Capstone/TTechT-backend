package com.example.TTECHT.controller.user;

import com.example.TTECHT.dto.request.ApiResponse;
import com.example.TTECHT.dto.request.ForgotPasswordRequest;
import com.example.TTECHT.dto.request.ResetPasswordRequest;
import com.example.TTECHT.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.initiateForgotPassword(request.getEmail());

        return ResponseEntity.ok(ApiResponse.<Void>builder().message("If an account with that email exists, we've sent password reset instructions.").build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponse.<Void>builder().message("Password has been reset successfully. You can now log in with your new password.").build());
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<ApiResponse<Void>> validateResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateToken(token);

        if (isValid) {
            return ResponseEntity.ok(ApiResponse.<Void>builder().message("Token is valid").build());
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.<Void>builder()
                    .message("Invalid or expired token")
                    .build()
            );
        }
    }
}