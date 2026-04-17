package com.huseyinsacikay.controller;

import com.huseyinsacikay.dto.request.PitchCreateRequest;
import com.huseyinsacikay.dto.response.PitchResponse;
import com.huseyinsacikay.handler.ApiError;
import com.huseyinsacikay.service.PitchService;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.huseyinsacikay.service.ReservationService;
import com.huseyinsacikay.dto.response.ReservationResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pitches")
@RequiredArgsConstructor
@Tag(name = "Pitches", description = "Protected pitch management endpoints. Only ADMIN users can create/delete pitches.")
@SecurityRequirement(name = "bearerAuth")
public class PitchController {

    private final PitchService pitchService;
    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a new pitch",
            description = "Creates a new pitch venue. ADMIN role required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pitch created successfully",
                    content = @Content(schema = @Schema(implementation = PitchResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Validation Error", value = """
                                    {
                                      "status": 400,
                                      "exception": {
                                        "code": "1009",
                                        "path": "/api/v1/pitches",
                                        "message": "Validation failed"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Only ADMIN users can create pitches",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "path": "/api/v1/pitches",
                                        "message": "Only ADMIN users can create pitches"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<PitchResponse> createPitch(
            @Parameter(
                    required = true,
                    description = "Pitch creation payload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PitchCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "Create pitch request",
                                    value = """
                                            {
                                              "name": "Camp Nou",
                                              "location": "Barcelona, Spain",
                                              "hourlyPrice": 100.00,
                                              "capacity": 22
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody PitchCreateRequest request
    ) {
        return ResponseEntity.ok(pitchService.createPitch(request));
    }

    @GetMapping
    @Operation(
            summary = "List all pitches (paginated)",
            description = "Returns a paginated list of all available pitches. Public endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pitch list returned successfully")
    })
    public ResponseEntity<Page<PitchResponse>> getAllPitches(
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(pitchService.getAllPitches(pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get pitch details by ID",
            description = "Returns detailed information about a specific pitch. Public endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pitch found",
                    content = @Content(schema = @Schema(implementation = PitchResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pitch not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "path": "/api/v1/pitches/...",
                                        "message": "Record not found"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<PitchResponse> getPitchById(@PathVariable UUID id) {
        return ResponseEntity.ok(pitchService.getPitchById(id));
    }

    @GetMapping("/{id}/reservations")
    @Operation(
            summary = "List reservations for a pitch",
            description = "Returns a paginated reservation history for the selected pitch. Public endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation page returned successfully")
    })
    public ResponseEntity<Page<ReservationResponse>> getReservationsByPitchId(
            @PathVariable UUID id,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(reservationService.getReservationsByPitchId(id, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a pitch",
            description = "Updates an existing pitch venue. ADMIN role required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pitch updated successfully",
                    content = @Content(schema = @Schema(implementation = PitchResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Validation Error", value = """
                                    {
                                      "status": 400,
                                      "exception": {
                                        "code": "1009",
                                        "path": "/api/v1/pitches/...",
                                        "message": "Validation failed"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Only ADMIN users can update pitches",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "path": "/api/v1/pitches/...",
                                        "message": "Only ADMIN users can update pitches"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Pitch not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "path": "/api/v1/pitches/...",
                                        "message": "Record not found"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<PitchResponse> updatePitch(
            @PathVariable UUID id,
            @Valid @RequestBody PitchCreateRequest request
    ) {
        return ResponseEntity.ok(pitchService.updatePitch(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a pitch",
            description = "Deletes a pitch venue. ADMIN role required. Associated reservations should be handled before deletion."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pitch deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Only ADMIN users can delete pitches",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "path": "/api/v1/pitches/...",
                                        "message": "Only ADMIN users can delete pitches"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Pitch not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "path": "/api/v1/pitches/...",
                                        "message": "Record not found"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<Void> deletePitch(@PathVariable UUID id) {
        pitchService.deletePitch(id);
        return ResponseEntity.noContent().build();
    }
}