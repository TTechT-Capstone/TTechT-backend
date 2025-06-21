package com.example.TTECHT.service;

import com.example.TTECHT.dto.repsonse.PermissionResponse;
import com.example.TTECHT.dto.request.PermissionRequest;

import java.util.List;

public interface PermissionService {
    PermissionResponse create(PermissionRequest request);
    List<PermissionResponse> getAll();
    void delete(String permission);
}
