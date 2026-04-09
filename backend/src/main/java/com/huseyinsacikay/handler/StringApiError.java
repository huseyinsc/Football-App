package com.huseyinsacikay.handler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
@Schema(name = "StringApiError", description = "Error response for general errors.")
public class StringApiError {
    @Schema(description = "HTTP status code", example = "401")
    private Integer status;

    @Schema(description = "Error details")
    private StringErrorDetails exception;
}

@Getter @Setter
@Schema(name = "StringErrorDetails", description = "Details of string-based errors.")
class StringErrorDetails {
    @Schema(description = "Error code", example = "1010")
    private String code;

    @Schema(description = "Request path", example = "/api/v1/auth/login")
    private String path;

    @Schema(description = "Error timestamp", example = "2026-04-10T16:40:11.406+03:00")
    private OffsetDateTime createTime;

    @Schema(description = "Error message", example = "Authentication is required to access this resource")
    private String message;
}