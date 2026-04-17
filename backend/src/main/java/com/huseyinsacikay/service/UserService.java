package com.huseyinsacikay.service;

import com.huseyinsacikay.dto.request.UserCreateRequest;
import com.huseyinsacikay.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);
    UserResponse getUserById(UUID id);
    UserResponse getUserByUsername(String username);
    UserResponse updateUser(UUID id, com.huseyinsacikay.dto.request.UserUpdateRequest request);
    Page<UserResponse> getAllUsers(Pageable pageable);
    void deleteUser(UUID id);
}
