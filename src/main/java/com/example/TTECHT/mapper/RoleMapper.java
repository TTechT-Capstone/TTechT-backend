package com.example.TTECHT.mapper;

import com.example.TTECHT.dto.repsonse.RoleResponse;
import com.example.TTECHT.dto.request.RoleRequest;
import com.example.TTECHT.entity.user.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
