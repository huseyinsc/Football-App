package com.huseyinsacikay.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class PitchResponse {
    private UUID id;
    private String name;
    private String location;
    private BigDecimal hourlyPrice;
    private Integer capacity;
    private boolean isAvailable;
}
