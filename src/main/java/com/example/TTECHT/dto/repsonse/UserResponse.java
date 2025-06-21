package com.example.TTECHT.dto.repsonse;

import com.example.TTECHT.constant.PredefinedRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
    String firstName;
    String lastName;
    List<RoleResponse> roles;
}
