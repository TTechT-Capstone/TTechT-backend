package com.example.TTECHT.dto.repsonse;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    private String token;
    private boolean isAuthenticated;
    private String role;
}
