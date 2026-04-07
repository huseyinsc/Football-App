package com.huseyinsacikay.service.impl;

import com.huseyinsacikay.dto.request.AuthRequest;
import com.huseyinsacikay.dto.request.UserCreateRequest;
import com.huseyinsacikay.dto.response.AuthResponse;
import com.huseyinsacikay.dto.response.UserResponse;
import com.huseyinsacikay.entity.User;
import com.huseyinsacikay.exception.MessageType;
import com.huseyinsacikay.exception.NotFoundException;
import com.huseyinsacikay.repository.UserRepository;
import com.huseyinsacikay.security.JwtService;
import com.huseyinsacikay.service.AuthService;
import com.huseyinsacikay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(UserCreateRequest request) {
        UserResponse createdUser = userService.createUser(request);
        User user = userRepository.findByUsername(createdUser.getUsername())
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
        String jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
        String jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
