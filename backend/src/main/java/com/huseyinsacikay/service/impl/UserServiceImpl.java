package com.huseyinsacikay.service.impl;

import com.huseyinsacikay.dto.request.UserCreateRequest;
import com.huseyinsacikay.dto.request.UserUpdateRequest;
import com.huseyinsacikay.dto.response.UserResponse;
import com.huseyinsacikay.entity.Role;
import com.huseyinsacikay.entity.User;
import com.huseyinsacikay.exception.ConflictException;
import com.huseyinsacikay.exception.MessageType;
import com.huseyinsacikay.exception.NotFoundException;
import com.huseyinsacikay.repository.UserRepository;
import com.huseyinsacikay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername()) || userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(MessageType.USER_ALREADY_EXISTS);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Role.USER)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
        return mapToResponse(user);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
        return mapToResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            throw new AccessDeniedException(MessageType.ACCESS_DENIED.getMessage());
        }

        if (currentUser.getId().equals(user.getId())) {
            // Updating own profile (cannot change role)
            if (request.getUsername() != null && !user.getUsername().equals(request.getUsername())) {
                if (userRepository.existsByUsername(request.getUsername())) throw new ConflictException(MessageType.USER_ALREADY_EXISTS);
                user.setUsername(request.getUsername());
            }
            if (request.getEmail() != null && !user.getEmail().equals(request.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) throw new ConflictException(MessageType.USER_ALREADY_EXISTS);
                user.setEmail(request.getEmail());
            }
            if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
            
        } else if (currentUser.getRole() == Role.ADMIN) {
            // Admin updating someone else (can only change role)
            if (request.getRole() != null) {
                user.setRole(request.getRole());
            }
        } else {
            throw new AccessDeniedException(MessageType.ACCESS_DENIED.getMessage());
        }

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MessageType.NO_RECORD_EXIST));
        userRepository.delete(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
