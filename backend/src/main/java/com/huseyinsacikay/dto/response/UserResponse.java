package com.huseyinsacikay.dto.response;

import com.huseyinsacikay.entity.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private Role role;
    private String phoneNumber;
    private boolean isActive;
    private OffsetDateTime createdAt;
}
