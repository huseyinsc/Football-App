package com.huseyinsacikay.dto.response;

import com.huseyinsacikay.entity.ReservationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ReservationResponse {
    private UUID id;
    private UUID userId;
    private String username;
    private UUID pitchId;
    private String pitchName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalPrice;
    private ReservationStatus status;
}
