package com.huseyinsacikay.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Schema(name = "ReservationCreateRequest", description = "Payload used to create a new reservation.")
public class ReservationCreateRequest {
    @Schema(description = "Identifier of the user who owns the reservation.", example = "11111111-1111-1111-1111-111111111111")
    @NotNull(message = "User ID is required")
    private UUID userId;

    @Schema(description = "Identifier of the pitch to reserve.", example = "22222222-2222-2222-2222-222222222222")
    @NotNull(message = "Pitch ID is required")
    private UUID pitchId;

    @Schema(description = "Reservation start date-time in ISO-8601 format.", example = "2026-05-01T18:00:00")
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @Schema(description = "Reservation end date-time in ISO-8601 format.", example = "2026-05-01T19:30:00")
    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    @AssertTrue(message = "End time must be after start time")
    public boolean isTimeRangeValid() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return endTime.isAfter(startTime);
    }
}
