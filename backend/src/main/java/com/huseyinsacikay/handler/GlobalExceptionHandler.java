package com.huseyinsacikay.handler;

import com.huseyinsacikay.exception.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiError<String>> handleBaseException(BaseException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(createApiError(ex.getMessage(), request, ex.getStatus()));
    }

    private <E> ApiError<E> createApiError(E message, HttpServletRequest request, HttpStatus status) {
        ApiError<E> apiError = new ApiError<>();
        apiError.setStatus(status.value());

        InternalException<E> internalException = new InternalException<>();
        internalException.setCreateTime(OffsetDateTime.now());
        internalException.setHostName(getHostname());
        internalException.setPath(request.getRequestURI());
        internalException.setMessage(message);

        apiError.setException(internalException);
        return apiError;
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown-Host";
        }
    }
}