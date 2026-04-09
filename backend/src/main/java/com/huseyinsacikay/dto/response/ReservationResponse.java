package com.huseyinsacikay.dto.response;

import com.huseyinsacikay.entity.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(name = "ReservationResponse", description = "Reservation data returned by protected reservation endpoints.")
public class ReservationResponse {
    @Schema(description = "Reservation identifier.", example = "33333333-3333-3333-3333-333333333333")
    private UUID id;
    @Schema(description = "Owner user identifier.", example = "11111111-1111-1111-1111-111111111111")
    private UUID userId;
    @Schema(description = "Owner username.", example = "astrofan")
    private String username;
    @Schema(description = "Pitch identifier.", example = "22222222-2222-2222-2222-222222222222")
    private UUID pitchId;
    @Schema(description = "Human-readable pitch name.", example = "Camp Nou")
    private String pitchName;
    @Schema(description = "Reservation start date-time.", example = "2026-05-01T18:00:00")
    private LocalDateTime startTime;
    @Schema(description = "Reservation end date-time.", example = "2026-05-01T19:30:00")
    private LocalDateTime endTime;
    @Schema(description = "Calculated total price for the reservation.", example = "150.00")
    private BigDecimal totalPrice;
    @Schema(description = "Current lifecycle status of the reservation.", example = "PENDING")
    private ReservationStatus status;
}
