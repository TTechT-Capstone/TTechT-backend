package com.example.TTECHT.service.impl;

import com.example.TTECHT.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class MailgunEmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name:Your App}")
    private String fromName;

    @Value("${mailgun.api.key:}")
    private String mailgunApiKey;

    @Value("${mailgun.domain:}")
    private String mailgunDomain;

    @Value("${mailgun.enabled:false}")
    private boolean mailgunApiEnabled;

    private static final Logger logger = LoggerFactory.getLogger(MailgunEmailServiceImpl.class);

    public MailgunEmailServiceImpl(JavaMailSender mailSender, RestTemplate restTemplate) {
        this.mailSender = mailSender;
        this.restTemplate = restTemplate;
    }

    @Override
    public void sendPasswordResetEmail(String email, String token, String firstName) {
        if (mailgunApiEnabled) {
            sendViaMailgunApi(email, token, firstName);
        } else {
            sendViaSmtp(email, token, firstName);
        }
    }

    @Override
    public void sendPasswordResetConfirmationEmail(String email, String firstName) {
        if (mailgunApiEnabled) {
            sendConfirmationViaMailgunApi(email, firstName);
        } else {
            sendConfirmationViaSmtp(email, firstName);
        }
    }

    // SMTP Method (fallback)
    private void sendViaSmtp(String email, String token, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(email);
            helper.setSubject("Password Reset Request");

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            String emailContent = buildPasswordResetEmailContent(firstName, resetLink);

            helper.setText(emailContent, true);

            mailSender.send(message);
            logger.info("Password reset email sent via SMTP to: {}", email);

        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("Failed to send password reset email via SMTP to: " + email, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private void sendConfirmationViaSmtp(String email, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(email);
            helper.setSubject("Password Reset Successful");

            String emailContent = buildPasswordResetConfirmationContent(firstName);
            helper.setText(emailContent, true);

            mailSender.send(message);
            logger.info("Password reset confirmation email sent via SMTP to: {}", email);

        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("Failed to send confirmation email via SMTP to: " + email, e);
            throw new RuntimeException("Failed to send confirmation email", e);
        }
    }

    // Mailgun API Methods (recommended)
    private void sendViaMailgunApi(String email, String token, String firstName) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("api", mailgunApiKey);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("from", fromName + " <" + fromEmail + ">");
            formData.add("to", email);
            formData.add("subject", "Password Reset Request");
            formData.add("html", buildPasswordResetEmailContent(firstName, resetLink));
            formData.add("text", buildPasswordResetTextContent(firstName, resetLink));

            // Mailgun-specific features
            formData.add("o:tag", "password-reset");
            formData.add("o:tracking", "true");
            formData.add("o:tracking-clicks", "true");
            formData.add("o:tracking-opens", "true");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            String url = "https://api.mailgun.net/v3/" + mailgunDomain + "/messages";
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Password reset email sent via Mailgun API to: {} - Response: {}",
                        email, response.getBody());
            } else {
                logger.error("Failed to send email via Mailgun API. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to send email via Mailgun API");
            }

        } catch (Exception e) {
            logger.error("Error sending password reset email via Mailgun API to: " + email, e);
            // Fallback to SMTP
            logger.info("Falling back to SMTP for email: {}", email);
            sendViaSmtp(email, token, firstName);
        }
    }

    private void sendConfirmationViaMailgunApi(String email, String firstName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("api", mailgunApiKey);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("from", fromName + " <" + fromEmail + ">");
            formData.add("to", email);
            formData.add("subject", "Password Reset Successful");
            formData.add("html", buildPasswordResetConfirmationContent(firstName));
            formData.add("text", buildPasswordResetConfirmationTextContent(firstName));

            // Mailgun-specific features
            formData.add("o:tag", "password-reset-confirmation");
            formData.add("o:tracking", "true");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

            String url = "https://api.mailgun.net/v3/" + mailgunDomain + "/messages";
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Password reset confirmation email sent via Mailgun API to: {} - Response: {}",
                        email, response.getBody());
            } else {
                logger.error("Failed to send confirmation email via Mailgun API. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to send confirmation email via Mailgun API");
            }

        } catch (Exception e) {
            logger.error("Error sending confirmation email via Mailgun API to: {}", email, e);
            // Fallback to SMTP
            logger.info("Falling back to SMTP for confirmation email: {}", email);
            sendConfirmationViaSmtp(email, firstName);
        }
    }

    private String buildPasswordResetEmailContent(String firstName, String resetLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset Request</title>
                <style>
                    .container { max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif; }
                    .header { background-color: #f8f9fa; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .button {
                        display: inline-block;
                        background-color: #007bff;
                        color: white;
                        padding: 12px 24px;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .footer { background-color: #f8f9fa; padding: 20px; font-size: 12px; color: #6c757d; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Password Reset Request</h1>
                    </div>
                    <div class="content">
                        <p>Hello %s,</p>
                        <p>We received a request to reset your password. Click the button below to reset it:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Reset Password</a>
                        </p>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; color: #007bff;">%s</p>
                        <p><strong>This link will expire in 24 hours.</strong></p>
                        <p>If you didn't request this password reset, please ignore this email or contact our support team if you have concerns.</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br>Your App Team</p>
                        <p>This is an automated message, please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName, resetLink, resetLink);
    }

    private String buildPasswordResetTextContent(String firstName, String resetLink) {
        return String.format("""
            Password Reset Request
            
            Hello %s,
            
            We received a request to reset your password. Click the link below to reset it:
            
            %s
            
            This link will expire in 24 hours.
            
            If you didn't request this password reset, please ignore this email.
            
            Best regards,
            Your App Team
            """, firstName, resetLink);
    }

    private String buildPasswordResetConfirmationContent(String firstName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset Successful</title>
                <style>
                    .container { max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif; }
                    .header { background-color: #d4edda; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .footer { background-color: #f8f9fa; padding: 20px; font-size: 12px; color: #6c757d; }
                    .success { color: #155724; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1 class="success">Password Reset Successful</h1>
                    </div>
                    <div class="content">
                        <p>Hello %s,</p>
                        <p>Your password has been successfully reset. You can now log in with your new password.</p>
                        <p>If you didn't make this change, please contact our support team immediately.</p>
                        <p>For your security, here are some tips:</p>
                        <ul>
                            <li>Use a strong, unique password</li>
                            <li>Don't share your password with anyone</li>
                            <li>Consider enabling two-factor authentication</li>
                        </ul>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br>Your App Team</p>
                        <p>This is an automated message, please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName);
    }

    private String buildPasswordResetConfirmationTextContent(String firstName) {
        return String.format("""
            Password Reset Successful
            
            Hello %s,
            
            Your password has been successfully reset. You can now log in with your new password.
            
            If you didn't make this change, please contact our support team immediately.
            
            Best regards,
            Your App Team
            """, firstName);
    }
}
