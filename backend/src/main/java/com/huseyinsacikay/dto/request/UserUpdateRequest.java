package com.huseyinsacikay.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    @Email(message = "Email must be valid")
    private String email;

    private String username;

    private String phoneNumber;

    private com.huseyinsacikay.entity.Role role;
}
