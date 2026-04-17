package com.huseyinsacikay.controller;

import com.huseyinsacikay.dto.request.ReservationCreateRequest;
import com.huseyinsacikay.dto.request.ReservationUpdateRequest;
import com.huseyinsacikay.dto.response.ReservationResponse;
import com.huseyinsacikay.dto.response.UserResponse;
import com.huseyinsacikay.handler.ApiError;
import com.huseyinsacikay.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Protected reservation management endpoints secured with JWT.")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("#request.userId == principal.id or hasRole('ADMIN')")
    @Operation(
            summary = "Create a reservation",
            description = "Creates a new pending reservation when the pitch is available and the requested time does not overlap another active booking."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation created successfully",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation or time-range error",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Validation Error", value = """
                                    {
                                      "status": 400,
                                      "exception": {
                                        "code": "1009",
                                        "path": "/api/v1/reservations",
                                        "message": "Validation failed"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "User is not allowed to create for another account",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "path": "/api/v1/reservations",
                                        "message": "User is not allowed to create for another account"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "User or pitch not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "path": "/api/v1/reservations",
                                        "message": "Record not found"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "Pitch unavailable or overlapping reservation exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Conflict error", value = """
                                    {
                                      "status": 409,
                                      "exception": {
                                        "code": "1002",
                                        "path": "/api/v1/reservations",
                                        "message": "Double booking detected"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<ReservationResponse> createReservation(
            @Parameter(
                    required = true,
                    description = "Reservation payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReservationCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "Create reservation",
                                    value = """
                                            {
                                              "userId": "11111111-1111-1111-1111-111111111111",
                                              "pitchId": "22222222-2222-2222-2222-222222222222",
                                              "startTime": "2026-05-01T18:00:00",
                                              "endTime": "2026-05-01T19:30:00"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        return ResponseEntity.ok(reservationService.createReservation(request));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a reservation by ID",
            description = "Returns the details of a specific reservation if the authenticated user owns it or has the ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation returned successfully"),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "path": "/api/v1/reservations/...",
                                        "message": "Record not found"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/{id}/users")
    @Operation(
            summary = "List users in a reservation",
            description = "Returns a list of all users participating in a reservation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users listed successfully")
    })
    public ResponseEntity<List<UserResponse>> getReservationUsers(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.getReservationUsers(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Cancel a reservation",
            description = "Cancels a reservation if the authenticated user owns it or has the ADMIN role. Completed and expired reservations cannot be cancelled."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reservation cancelled successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Reservation belongs to another user",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "path": "/api/v1/reservations/...",
                                        "message": "Reservation belongs to another user"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "path": "/api/v1/reservations/...",
                                        "message": "Record not found"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "Reservation cannot be cancelled in its current state",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Conflict error", value = """
                                    {
                                      "status": 409,
                                      "exception": {
                                        "code": "1002",
                                        "path": "/api/v1/reservations/...",
                                        "message": "Reservation cannot be cancelled in its current state"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<Void> cancelReservation(@PathVariable UUID id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a reservation",
            description = "Updates a reservation's start and end times if the authenticated user owns it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation updated successfully",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation or time-range error",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Validation Error", value = """
                                    {
                                      "status": 400,
                                      "exception": {
                                        "code": "1009",
                                        "path": "/api/v1/reservations/...",
                                        "message": "Validation failed"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Reservation belongs to another user",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "path": "/api/v1/reservations/...",
                                        "message": "Reservation belongs to another user"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "path": "/api/v1/reservations/...",
                                        "message": "Record not found"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "Overlapping reservation exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Conflict error", value = """
                                    {
                                      "status": 409,
                                      "exception": {
                                        "code": "1002",
                                        "path": "/api/v1/reservations/...",
                                        "message": "Double booking detected"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable UUID id,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        return ResponseEntity.ok(reservationService.updateReservation(id, request));
    }
}
