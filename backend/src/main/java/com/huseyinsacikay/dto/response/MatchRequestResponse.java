package com.huseyinsacikay.dto.response;

import com.huseyinsacikay.entity.MatchRequestStatus;
import com.huseyinsacikay.entity.MatchRequestType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class MatchRequestResponse {
    private UUID id;
    private UUID reservationId;
    private UserResponse user;
    private MatchRequestType type;
    private MatchRequestStatus status;
    private LocalDateTime createdAt;
}
