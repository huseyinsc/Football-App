package com.huseyinsacikay.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ReservationCreateRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Pitch ID is required")
    private UUID pitchId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
}
