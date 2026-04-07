package com.huseyinsacikay.service;

import com.huseyinsacikay.dto.request.AuthRequest;
import com.huseyinsacikay.dto.request.UserCreateRequest;
import com.huseyinsacikay.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(UserCreateRequest request);
    AuthResponse authenticate(AuthRequest request);
}
