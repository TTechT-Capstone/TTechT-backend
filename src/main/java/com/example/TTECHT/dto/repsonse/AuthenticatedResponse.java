package com.example.TTECHT.dto.repsonse;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticatedResponse {
    private String token;
    private boolean isAuthenticated;
    private Collection<? extends GrantedAuthority> authorities;
}
