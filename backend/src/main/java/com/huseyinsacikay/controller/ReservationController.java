package com.huseyinsacikay.controller;

import com.huseyinsacikay.dto.request.ReservationCreateRequest;
import com.huseyinsacikay.dto.response.ReservationResponse;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Protected reservation management endpoints secured with JWT.")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("#request.userId == principal.id or hasRole('ADMIN')")
    @Operation(
            summary = "Create a reservation",
            description = "Creates a new pending reservation when the pitch is available and the requested time does not overlap another active booking.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation created successfully",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation or time-range error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "User is not allowed to create for another account",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "User or pitch not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Pitch unavailable or overlapping reservation exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ReservationResponse> createReservation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
            description = "Returns the details of a specific reservation if the authenticated user owns it or has the ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation returned successfully"),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
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
    public ResponseEntity<List<com.huseyinsacikay.dto.response.UserResponse>> getReservationUsers(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.getReservationUsers(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Cancel a reservation",
            description = "Cancels a reservation if the authenticated user owns it or has the ADMIN role. Completed and expired reservations cannot be cancelled.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reservation cancelled successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Reservation belongs to another user",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Reservation cannot be cancelled in its current state",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> cancelReservation(@PathVariable UUID id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a reservation",
            description = "Updates a reservation's start and end times if the authenticated user owns it.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation updated successfully",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation or time-range error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Reservation belongs to another user",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Overlapping reservation exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable UUID id,
            @Valid @RequestBody com.huseyinsacikay.dto.request.ReservationUpdateRequest request
    ) {
        return ResponseEntity.ok(reservationService.updateReservation(id, request));
    }

    @PostMapping("/{id}/join")
    @Operation(
            summary = "Join a reservation",
            description = "Joins an existing reservation as a participant.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully joined reservation",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Pitch is full or already joined",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ReservationResponse> joinReservation(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.joinReservation(id));
    }
}
