package com.huseyinsacikay.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AuthResponse", description = "Authentication result that contains the issued JWT token.")
public class AuthResponse {
    @Schema(description = "Signed JWT bearer token.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
    @Schema(description = "Username associated with the authenticated token.", example = "astrofan")
    private String username;
    @Schema(description = "Resolved application role of the authenticated user.", example = "USER")
    private String role;
}
