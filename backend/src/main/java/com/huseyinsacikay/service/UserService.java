package com.huseyinsacikay.service;

import com.huseyinsacikay.dto.request.UserCreateRequest;
import com.huseyinsacikay.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    UserResponse getUserById(UUID id);
    UserResponse getUserByUsername(String username);  // ADD THIS
    UserResponse updateUser(UUID id, com.huseyinsacikay.dto.request.UserUpdateRequest request);
    List<UserResponse> getAllUsers();
    void deleteUser(UUID id);
}
