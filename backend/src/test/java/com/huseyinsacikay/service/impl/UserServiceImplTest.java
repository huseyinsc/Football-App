package com.huseyinsacikay.service.impl;

import com.huseyinsacikay.dto.request.UserCreateRequest;
import com.huseyinsacikay.dto.response.UserResponse;
import com.huseyinsacikay.entity.Role;
import com.huseyinsacikay.entity.User;
import com.huseyinsacikay.exception.ConflictException;
import com.huseyinsacikay.exception.NotFoundException;
import com.huseyinsacikay.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserCreateRequest createRequest;
    private User mockUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        createRequest = new UserCreateRequest();
        createRequest.setUsername("testuser");
        createRequest.setEmail("test@example.com");
        createRequest.setPassword("password");
        createRequest.setPhoneNumber("1234567890");

        mockUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .phoneNumber("1234567890")
                .isActive(true)
                .build();
    }

    @Test
    void createUser_ShouldReturnUserResponse_WhenSuccessful() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponse response = userService.createUser(createRequest);

        assertNotNull(response);
        assertEquals(createRequest.getUsername(), response.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowConflictException_WhenUserExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(createRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUserResponse_WhenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        UserResponse response = userService.getUserById(userId);

        assertNotNull(response);
        assertEquals(userId, response.getId());
    }

    @Test
    void getUserById_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUserResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> usersPage = new PageImpl<>(List.of(mockUser), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(usersPage);

        Page<UserResponse> responses = userService.getAllUsers(pageable);

        assertNotNull(responses);
        assertEquals(1, responses.getTotalElements());
        assertEquals("testuser", responses.getContent().get(0).getUsername());
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        doNothing().when(userRepository).delete(mockUser);

        assertDoesNotThrow(() -> userService.deleteUser(userId));
        verify(userRepository).delete(mockUser);
    }
}