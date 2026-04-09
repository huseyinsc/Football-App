package com.huseyinsacikay.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(name = "PitchResponse", description = "Pitch/venue information returned by pitch management endpoints.")
public class PitchResponse {
    
    @Schema(description = "Unique pitch identifier", example = "22222222-2222-2222-2222-222222222222")
    private UUID id;
    
    @Schema(description = "Human-readable pitch name", example = "Camp Nou")
    private String name;
    
    @Schema(description = "Geographic location of the pitch", example = "Barcelona, Spain")
    private String location;
    
    @Schema(description = "Hourly rental price in currency units", example = "100.00")
    private BigDecimal hourlyPrice;
    
    @Schema(description = "Player capacity (match type, e.g., 6v6=12, 7v7=14, 11v11=22)", example = "22")
    private Integer capacity;
    
    @Schema(description = "Whether the pitch is currently available for bookings", example = "true")
    private boolean isAvailable;
}
