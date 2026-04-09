package com.huseyinsacikay.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AuthRequest", description = "Credentials used to authenticate an existing user.")
public class AuthRequest {
    @Schema(description = "Unique username of the registered user.", example = "astrofan")
    @NotBlank(message = "Username cannot be empty")
    private String username;
    @Schema(description = "Raw password supplied during login.", example = "secret123")
    @NotBlank(message = "Password cannot be empty")
    private String password;
}
