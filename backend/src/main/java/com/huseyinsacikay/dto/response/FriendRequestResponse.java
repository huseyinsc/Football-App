package com.huseyinsacikay.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class FriendRequestResponse {
    private UUID id;
    private UserResponse sender;
    private UserResponse receiver;
    private LocalDateTime createdAt;
}
