package com.huseyinsacikay.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "UserCreateRequest", description = "Payload used to register a new application user.")
public class UserCreateRequest {
    
    @Schema(description = "Unique username for login and display. 3-20 characters, alphanumeric and underscores only.", 
            example = "astrofan")
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @Schema(description = "Unique email address of the user.", example = "astrofan@example.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "Raw password to be encoded and stored securely. Minimum 8 characters with at least one uppercase, one lowercase, one number, and one special character.", 
            example = "SecurePass123!")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)"
    )
    private String password;

    @Schema(description = "Optional phone number in international format (e.g., +905551112233).", 
            example = "+905551112233")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be in valid international format")
    private String phoneNumber;
}
