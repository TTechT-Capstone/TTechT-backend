package com.example.TTECHT.service;

import com.example.TTECHT.dto.repsonse.RoleResponse;
import com.example.TTECHT.dto.request.RoleRequest;

import java.util.List;

public interface RoleService {
    RoleResponse create(RoleRequest request);
    List<RoleResponse> getAll();
    void delete(Long roleID);
}
