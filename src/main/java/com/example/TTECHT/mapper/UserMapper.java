package com.example.TTECHT.mapper;


import com.example.TTECHT.dto.repsonse.UserResponse;
import com.example.TTECHT.dto.request.UserCreationRequest;
import com.example.TTECHT.dto.request.UserUpdateRequest;
import com.example.TTECHT.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest userCreationRequest);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest userUpdateRequest);
}
