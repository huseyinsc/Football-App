package com.huseyinsacikay.controller;

import com.huseyinsacikay.dto.response.UserResponse;
import com.huseyinsacikay.dto.request.UserUpdateRequest;
import com.huseyinsacikay.handler.ApiError;
import com.huseyinsacikay.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.huseyinsacikay.service.ReservationService;
import com.huseyinsacikay.dto.response.ReservationResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Protected user profile management endpoints.")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final ReservationService reservationService;

    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Returns the authenticated user's profile information."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        // Get the user UUID from the principal (requires custom extraction logic or UserDetails modification)
        // This is a simplified version - in production, store UUID in UserDetails
        UserResponse currentUser = userService.getUserByUsername(username);
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user profile by ID",
            description = "Returns a user's profile information. Users can only view their own profile unless they have ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "You can only view your own profile",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "path": "/api/v1/users/...",
                                        "message": "You can only view your own profile"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "path": "/api/v1/users/...",
                                        "message": "Record not found"
                                      }
                                    }
                                    """)))
    })
    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List all users",
            description = "Returns a list of all registered users. ADMIN role required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User list retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Only ADMIN users can list all users",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "path": "/api/v1/users",
                                        "message": "Only ADMIN users can list all users"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @ParameterObject @PageableDefault(size = 20, sort = "username") Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    @Operation(
            summary = "Delete a user account",
            description = "Deletes a user account. Users can delete their own account or ADMINs can delete any account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User account deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "You can only delete your own account",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "path": "/api/v1/users/...",
                                        "message": "You can only delete your own account"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "path": "/api/v1/users/...",
                                        "message": "Record not found"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/reservations")
    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    @Operation(
            summary = "List reservations by user",
            description = "Returns a paginated reservation history. Users can only list their own records."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation page returned successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "User is not allowed to access another user's history",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "path": "/api/v1/users/.../reservations",
                                        "message": "User is not allowed to access another user's history"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<Page<ReservationResponse>> getReservationsByUserId(
            @PathVariable UUID id,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(reservationService.getReservationsByUserId(id, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    @Operation(
            summary = "Update user profile",
            description = "Updates a user's profile information. Users can only update their own profile unless they have ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Validation Error", value = """
                                    {
                                      "status": 400,
                                      "exception": {
                                        "code": "1009",
                                        "path": "/api/v1/users/...",
                                        "message": "Validation failed"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "You can only update your own profile",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "path": "/api/v1/users/...",
                                        "message": "You can only update your own profile"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "path": "/api/v1/users/...",
                                        "message": "Record not found"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @jakarta.validation.Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}