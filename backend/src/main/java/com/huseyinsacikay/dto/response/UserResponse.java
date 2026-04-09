package com.huseyinsacikay.dto.response;

import com.huseyinsacikay.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(name = "UserResponse", description = "User profile information returned by protected endpoints.")
public class UserResponse {
    
    @Schema(description = "Unique user identifier", example = "11111111-1111-1111-1111-111111111111")
    private UUID id;
    
    @Schema(description = "Unique username for the user account", example = "astrofan")
    private String username;
    
    @Schema(description = "Email address associated with the account", example = "astrofan@example.com")
    private String email;
    
    @Schema(description = "User's application role", example = "USER")
    private Role role;
    
    @Schema(description = "Phone number in international format", example = "+905551112233")
    private String phoneNumber;
    
    @Schema(description = "Whether the user account is active", example = "true")
    private boolean isActive;
    
    @Schema(description = "User account creation timestamp", example = "2026-04-09T13:25:08.397+03:00")
    private OffsetDateTime createdAt;
}
