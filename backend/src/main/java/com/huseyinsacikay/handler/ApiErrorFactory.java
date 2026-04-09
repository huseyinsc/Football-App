package com.huseyinsacikay.handler;

import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

public final class ApiErrorFactory {

    private ApiErrorFactory() {
    }

    public static <E> ApiError<E> create(String code, E message, String path, HttpStatus status) {
        ApiError<E> apiError = new ApiError<>();
        apiError.setStatus(status.value());

        InternalException<E> internalException = new InternalException<>();
        internalException.setCreateTime(OffsetDateTime.now());
        internalException.setCode(code);
        internalException.setPath(path);
        internalException.setMessage(message);

        apiError.setException(internalException);
        return apiError;
    }
}
