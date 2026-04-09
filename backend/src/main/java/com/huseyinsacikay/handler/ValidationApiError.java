package com.huseyinsacikay.handler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter @Setter
@Schema(name = "ValidationApiError", description = "Error response for validation failures.")
public class ValidationApiError {
    @Schema(description = "HTTP status code", example = "400")
    private Integer status;

    @Schema(description = "Error details")
    private ValidationErrorDetails exception;
}

@Getter @Setter
@Schema(name = "ValidationErrorDetails", description = "Details of validation errors.")
class ValidationErrorDetails {
    @Schema(description = "Error code", example = "1009")
    private String code;

    @Schema(description = "Request path", example = "/api/v1/auth/register")
    private String path;

    @Schema(description = "Error timestamp", example = "2026-04-10T16:40:11.406+03:00")
    private OffsetDateTime createTime;

    @Schema(description = "Field validation errors", example = "{\"password\":\"Password too weak\",\"email\":\"Invalid email format\"}")
    private Map<String, String> message;
}