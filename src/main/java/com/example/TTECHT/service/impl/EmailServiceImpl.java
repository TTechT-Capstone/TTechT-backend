package com.example.TTECHT.service.impl;

import com.example.TTECHT.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class EmailServiceImpl implements EmailService {

    JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    String frontendUrl;

    @Value("${app.email.from}")
    String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String email, String token, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Password Reset Request");

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            String emailContent = buildPasswordResetEmailContent(firstName, resetLink);

            helper.setText(emailContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    public void sendPasswordResetConfirmationEmail(String email, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Password Reset Successful");

            String emailContent = buildPasswordResetConfirmationContent(firstName);
            helper.setText(emailContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send confirmation email", e);
        }
    }


    private String buildPasswordResetEmailContent(String firstName, String resetLink) {
        return String.format("""
            <html>
            <body>
                <h2>Password Reset Request</h2>
                <p>Hello %s,</p>
                <p>We received a request to reset your password. Click the link below to reset it:</p>
                <p><a href="%s" style="background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Reset Password</a></p>
                <p>This link will expire in 24 hours.</p>
                <p>If you didn't request this, please ignore this email.</p>
                <p>Best regards,<br>Your App Team</p>
            </body>
            </html>
            """, firstName, resetLink);
    }


    private String buildPasswordResetConfirmationContent(String firstName) {
        return String.format("""
            <html>
            <body>
                <h2>Password Reset Successful</h2>
                <p>Hello %s,</p>
                <p>Your password has been successfully reset.</p>
                <p>If you didn't make this change, please contact our support team immediately.</p>
                <p>Best regards,<br>Your App Team</p>
            </body>
            </html>
            """, firstName);
    }
}
