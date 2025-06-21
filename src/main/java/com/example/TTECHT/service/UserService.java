package com.example.TTECHT.service;

import com.example.TTECHT.dto.repsonse.UserResponse;
import com.example.TTECHT.dto.request.UserCreationRequest;
import com.example.TTECHT.dto.request.UserUpdateRequest;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreationRequest request);
    UserResponse getMyInfo();
    UserResponse updateUser(String userId, UserUpdateRequest request);
    void deleteUser(String userId);
    List<UserResponse> getUsers();
    UserResponse getUser(String id);
}
