package com.huseyinsacikay.controller;

import com.huseyinsacikay.dto.request.AuthRequest;
import com.huseyinsacikay.dto.request.UserCreateRequest;
import com.huseyinsacikay.dto.response.AuthResponse;
import com.huseyinsacikay.handler.StringApiError;
import com.huseyinsacikay.handler.ValidationApiError;
import com.huseyinsacikay.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Public endpoints for user registration and login.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new active USER account and immediately returns a JWT token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ValidationApiError.class))),
            @ApiResponse(responseCode = "409", description = "Username or email already exists",
                    content = @Content(schema = @Schema(implementation = StringApiError.class)))
    })
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Registration payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "Register request",
                                    value = """
                                            {
                                              "username": "astrofan",
                                              "email": "astrofan@example.com",
                                              "password": "secret123",
                                              "phoneNumber": "+905551112233"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody UserCreateRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate a user",
            description = "Validates credentials and returns a JWT token for subsequent protected requests."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ValidationApiError.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = StringApiError.class)))
    })
    public ResponseEntity<AuthResponse> authenticate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Login payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthRequest.class),
                            examples = @ExampleObject(
                                    name = "Login request",
                                    value = """
                                            {
                                              "username": "astrofan",
                                              "password": "secret123"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody AuthRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}
