package com.huseyinsacikay.controller;

import com.huseyinsacikay.dto.response.MatchRequestResponse;
import com.huseyinsacikay.handler.ApiError;
import com.huseyinsacikay.service.MatchRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/match-requests")
@RequiredArgsConstructor
@Tag(name = "Match Requests API", description = "Endpoints for match joining and invitations. All endpoints require authentication.")
@SecurityRequirement(name = "bearerAuth")
public class MatchRequestController {

    private final MatchRequestService matchRequestService;

    @PostMapping("/reservations/{reservationId}/join")
    @Operation(
            summary = "Request to Join a Match",
            description = """
                    Submits a join request for the specified reservation.
                    - **PUBLIC**: Any authenticated user can request.
                    - **FRIENDS_ONLY**: Only friends of the organizer can request.
                    - **INVITE_ONLY**: Join requests are not allowed — only organizer invites work.
                    
                    The organizer must accept the request before the user joins the participants list.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Join request submitted, awaiting organizer approval"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Unauthorized error", value = """
                                    {
                                      "status": 401,
                                      "exception": {
                                        "code": "1010",
                                        "message": "Authentication is required to access this resource",
                                        "path": "/api/v1/match-requests/reservations/.../join"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Policy violation: invite-only match or non-friend on friends-only match",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Policy violation error", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "message": "Interaction blocked or Policy violation",
                                        "path": "/api/v1/match-requests/reservations/.../join"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found error", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "message": "Record not found",
                                        "path": "/api/v1/match-requests/reservations/.../join"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "Already in the match or request already pending",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Conflict error", value = """
                                    {
                                      "status": 409,
                                      "exception": {
                                        "code": "1009",
                                        "message": "Request validation failed",
                                        "path": "/api/v1/match-requests/reservations/.../join"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<Void> requestToJoin(
            @Parameter(description = "UUID of the reservation to request joining", required = true,
                    example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable UUID reservationId) {
        matchRequestService.requestToJoin(reservationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reservations/{reservationId}/invite/{userId}")
    @Operation(
            summary = "Invite a User to a Match",
            description = "Sends a match invite to the specified user. Only the organizer of the reservation can invite users. Works for any JoinPolicy including INVITE_ONLY."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invite sent, awaiting user acceptance"),
            @ApiResponse(responseCode = "403", description = "Only the organizer can send invites",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied error", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "message": "You are not allowed to access this reservation",
                                        "path": "/api/v1/match-requests/reservations/.../invite/..."
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Reservation or user not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found error", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "message": "Record not found",
                                        "path": "/api/v1/match-requests/reservations/.../invite/..."
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<Void> inviteToMatch(
            @Parameter(description = "UUID of the reservation", required = true,
                    example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable UUID reservationId,
            @Parameter(description = "UUID of the user to invite", required = true,
                    example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID userId) {
        matchRequestService.inviteToMatch(reservationId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/accept")
    @Operation(
            summary = "Accept a Join Request or Match Invite",
            description = """
                    Accepts a pending match request.
                    - **JOIN_REQUEST**: Only the organizer can accept. User is added to participants.
                    - **MATCH_INVITE**: Only the invited user can accept. User is added to participants.
                    
                    On acceptance, the request is deleted and the user appears in the reservation's participant list.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request accepted, user added to reservation participants"),
            @ApiResponse(responseCode = "400", description = "Request is not in PENDING state",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Invalid state error", value = """
                                    {
                                      "status": 400,
                                      "exception": {
                                        "code": "1009",
                                        "message": "Request validation failed",
                                        "path": "/api/v1/match-requests/.../accept"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "You are not authorized to accept this request",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied error", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "message": "You are not allowed to access this reservation",
                                        "path": "/api/v1/match-requests/.../accept"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Request not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found error", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "message": "Record not found",
                                        "path": "/api/v1/match-requests/.../accept"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<Void> acceptRequest(
            @Parameter(description = "UUID of the match request to accept", required = true,
                    example = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
            @PathVariable UUID requestId) {
        matchRequestService.acceptRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/reject")
    @Operation(
            summary = "Reject a Join Request or Match Invite",
            description = """
                    Rejects a pending match request.
                    - **JOIN_REQUEST**: Only the organizer can reject.
                    - **MATCH_INVITE**: Only the invited user can reject.
                    
                    Rejected requests are marked as REJECTED and kept for record-keeping.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request rejected"),
            @ApiResponse(responseCode = "403", description = "You are not authorized to reject this request",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied error", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "message": "You are not allowed to access this reservation",
                                        "path": "/api/v1/match-requests/.../reject"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Request not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found error", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "message": "Record not found",
                                        "path": "/api/v1/match-requests/.../reject"
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<Void> rejectRequest(
            @Parameter(description = "UUID of the match request to reject", required = true,
                    example = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
            @PathVariable UUID requestId) {
        matchRequestService.rejectRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reservations/{reservationId}")
    @Operation(
            summary = "List Pending Join Requests for a Reservation",
            description = "Returns all pending JOIN_REQUEST entries for the given reservation. Only the organizer can view this list."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending join requests returned successfully",
                    content = @Content(schema = @Schema(implementation = MatchRequestResponse.class))),
            @ApiResponse(responseCode = "403", description = "Only the organizer can view join requests",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied error", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "message": "You are not allowed to access this reservation",
                                        "path": "/api/v1/match-requests/reservations/..."
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found error", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "message": "Record not found",
                                        "path": "/api/v1/match-requests/reservations/..."
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<Page<MatchRequestResponse>> getPendingRequestsForReservation(
            @Parameter(description = "UUID of the reservation", required = true,
                    example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable UUID reservationId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(matchRequestService.getPendingRequestsForReservation(reservationId, pageable));
    }

    @GetMapping("/me")
    @Operation(
            summary = "List My Pending Match Invites",
            description = "Returns all pending MATCH_INVITE entries sent to the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending invites returned successfully",
                    content = @Content(schema = @Schema(implementation = MatchRequestResponse.class)))
    })
    public ResponseEntity<Page<MatchRequestResponse>> getPendingInvitesForUser(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(matchRequestService.getPendingInvitesForUser(pageable));
    }
}
