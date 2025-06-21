package com.example.TTECHT.mapper;

import com.example.TTECHT.dto.repsonse.PermissionResponse;
import com.example.TTECHT.dto.request.PermissionRequest;
import com.example.TTECHT.entity.user.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}