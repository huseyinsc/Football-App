package com.huseyinsacikay.controller;

import com.huseyinsacikay.dto.response.FriendRequestResponse;
import com.huseyinsacikay.dto.response.UserResponse;
import com.huseyinsacikay.handler.ApiError;
import com.huseyinsacikay.service.ContactService;
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
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
@Tag(name = "Contacts API", description = "Endpoints for managing friends and contacts. All endpoints require authentication.")
@SecurityRequirement(name = "bearerAuth")
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/request/{userId}")
    @Operation(
            summary = "Send Friend Request",
            description = "Sends a friend request to the target user. If a reverse request already exists, both users are automatically connected as contacts. Blocked users cannot receive requests."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request sent (or auto-accepted if reverse request existed)"),
            @ApiResponse(responseCode = "400", description = "Cannot send request to yourself",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Self request error", value = """
                                    {
                                      "status": 400,
                                      "exception": {
                                        "code": "1009",
                                        "message": "Request validation failed",
                                        "path": "/api/v1/contacts/request/..."
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Authentication required", value = """
                                    {
                                      "status": 401,
                                      "exception": {
                                        "code": "1010",
                                        "path": "/api/v1/contacts/request/...",
                                        "message": "Authentication is required to access this resource"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "Interaction blocked between users",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Blocked interaction error", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "message": "Interaction blocked.",
                                        "path": "/api/v1/contacts/request/..."
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "Already contacts or request already pending",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Conflict error", value = """
                                    {
                                      "status": 409,
                                      "exception": {
                                        "code": "1009",
                                        "message": "Request validation failed",
                                        "path": "/api/v1/contacts/request/..."
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<Void> sendFriendRequest(
            @Parameter(description = "UUID of the user to send a request to", required = true,
                    example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID userId) {
        contactService.sendFriendRequest(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accept/{requestId}")
    @Operation(
            summary = "Accept Friend Request",
            description = "Accepts an incoming friend request. Both users are added to each other's contact list. Only the receiver of the request can accept it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Friend request accepted, users are now contacts"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Only the receiver can accept this request",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied error", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "message": "You are not allowed to access this reservation",
                                        "path": "/api/v1/contacts/accept/..."
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Friend request not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found error", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "message": "Record not found",
                                        "path": "/api/v1/contacts/accept/..."
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<Void> acceptFriendRequest(
            @Parameter(description = "UUID of the friend request to accept", required = true,
                    example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable UUID requestId) {
        contactService.acceptFriendRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject/{requestId}")
    @Operation(
            summary = "Reject Friend Request",
            description = "Rejects an incoming friend request. A strike is recorded against the sender. If the sender has accumulated 2 strikes (rejections or timeouts), both users are automatically blocked."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Friend request rejected"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Only the receiver can reject this request",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Access denied", value = """
                                    {
                                      "status": 403,
                                      "exception": {
                                        "code": "1007",
                                        "path": "/api/v1/contacts/reject/...",
                                        "message": "Only the receiver can reject this request"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Friend request not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found error", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "message": "Record not found",
                                        "path": "/api/v1/contacts/reject/..."
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<Void> rejectFriendRequest(
            @Parameter(description = "UUID of the friend request to reject", required = true,
                    example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            @PathVariable UUID requestId) {
        contactService.rejectFriendRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    @Operation(
            summary = "Remove Contact",
            description = "Removes the target user from your contacts list. This is mutual — the target user also loses you as a contact."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contact removed from both users' lists"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> removeContact(
            @Parameter(description = "UUID of the user to remove from contacts", required = true,
                    example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID userId) {
        contactService.removeContact(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/block/{userId}")
    @Operation(
            summary = "Block User",
            description = "Blocks a user. This is mutual — both users lose the ability to interact (send requests, etc.). Any existing contact or pending request is removed."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User blocked successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot block yourself",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Self block error", value = """
                                    {
                                      "status": 400,
                                      "exception": {
                                        "code": "1009",
                                        "path": "/api/v1/contacts/block/...",
                                        "message": "Validation failed"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(name = "Not found error", value = """
                                    {
                                      "status": 404,
                                      "exception": {
                                        "code": "1001",
                                        "message": "Record not found",
                                        "path": "/api/v1/contacts/block/..."
                                      }
                                    }
                                    """)))
    })
    public ResponseEntity<Void> blockUser(
            @Parameter(description = "UUID of the user to block", required = true,
                    example = "11111111-1111-1111-1111-111111111111")
            @PathVariable UUID userId) {
        contactService.blockUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(
            summary = "List My Contacts",
            description = "Returns a paginated list of users you are friends with."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contact list returned successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Page<UserResponse>> getContacts(
            @ParameterObject @PageableDefault(size = 20, sort = "username") Pageable pageable) {
        return ResponseEntity.ok(contactService.getContacts(pageable));
    }

    @GetMapping("/requests/incoming")
    @Operation(
            summary = "List Incoming Friend Requests",
            description = "Returns a paginated list of pending friend requests sent to the current user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incoming request list returned successfully",
                    content = @Content(schema = @Schema(implementation = FriendRequestResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Page<FriendRequestResponse>> getIncomingRequests(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contactService.getPendingIncomingRequests(pageable));
    }

    @GetMapping("/requests/outgoing")
    @Operation(
            summary = "List Outgoing Friend Requests",
            description = "Returns a paginated list of pending friend requests sent by the current user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Outgoing request list returned successfully",
                    content = @Content(schema = @Schema(implementation = FriendRequestResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Page<FriendRequestResponse>> getOutgoingRequests(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(contactService.getPendingOutgoingRequests(pageable));
    }
}
