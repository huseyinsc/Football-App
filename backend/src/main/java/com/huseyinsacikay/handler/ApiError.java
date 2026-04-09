package com.huseyinsacikay.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
@Schema(name = "ApiError", description = "Standard error envelope returned by the API.")
public class ApiError<E> {
    @Schema(description = "HTTP status code of the error response.", example = "400")
    private Integer status;

    @Schema(description = "Error details payload.")
    private InternalException<E> exception;
}

@Getter @Setter
@Schema(name = "ApiErrorDetails", description = "Structured details describing an API error.")
class InternalException<E> {
    @Schema(description = "Application-specific error code.", example = "1009")
    private String code;

    @Schema(description = "Request path that produced the error.", example = "/api/v1/auth/register")
    private String path;

    @Schema(description = "Server-side error creation time.", example = "2026-04-10T16:39:26.123+03:00")
    private OffsetDateTime createTime;

    @Schema(description = "Human-readable message or field-error object depending on the failure type.",
            oneOf = {String.class, Object.class})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private E message;
}
