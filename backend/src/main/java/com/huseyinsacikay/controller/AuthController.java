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
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
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
                    content = @Content(schema = @Schema(implementation = ValidationApiError.class),
                            examples = @ExampleObject(name = "Validation Error", value = """
                                    {
                                      "status": 400,
                                      "exception": {
                                        "code": "1009",
                                        "path": "/api/v1/auth/register",
                                        "message": {
                                          "username": "Username must be between 3 and 20 characters",
                                          "password": "Password is required"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "Username or email already exists",
                    content = @Content(schema = @Schema(implementation = StringApiError.class),
                            examples = @ExampleObject(name = "Conflict Error", value = """
                                    {
                                      "status": 409,
                                      "exception": {
                                        "code": "1002",
                                        "path": "/api/v1/auth/register",
                                        "message": "User already exists"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<AuthResponse> register(
            @Parameter(
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
        log.info("Process: Incoming registration request. Username: {}, Email: {}", request.getUsername(), request.getEmail());
        AuthResponse response = authService.register(request);
        log.info("Process: Registration successful. Token generated for user: {}", response.getUsername());
        return ResponseEntity.ok(response);
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
                    content = @Content(schema = @Schema(implementation = ValidationApiError.class),
                            examples = @ExampleObject(name = "Validation Error", value = """
                                    {
                                      "status": 400,
                                      "exception": {
                                        "code": "1009",
                                        "path": "/api/v1/auth/login",
                                        "message": {
                                          "username": "Username is required"
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = StringApiError.class)))
    })
    public ResponseEntity<AuthResponse> authenticate(
            @Parameter(
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
        log.info("Process: Incoming login request. Username: {}", request.getUsername());
        AuthResponse response = authService.authenticate(request);
        log.info("Process: Login successful. Token generated for user: {}", response.getUsername());
        return ResponseEntity.ok(response);
    }
}
