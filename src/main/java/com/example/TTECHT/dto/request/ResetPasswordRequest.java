package com.example.TTECHT.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {

    @NotBlank(message = "Token cannot be blank")
    String token;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 6, message = "New password must be at least 6 characters long")
            // FIXME: For test purposes, we are not using the regex pattern for password validation
//    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].*$",
//            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    String newPassword;

    @NotBlank(message = "Confirm new password cannot be blank")
    String confirmNewPassword;


}
