package com.huseyinsacikay.handler;

import com.huseyinsacikay.exception.BaseException;
import com.huseyinsacikay.exception.MessageType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiError<String>> handleBaseException(BaseException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiErrorFactory.create(ex.getMessageType().getCode(), ex.getMessage(), request.getRequestURI(), ex.getStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorFactory.create(
                        MessageType.VALIDATION_ERROR.getCode(),
                        validationErrors,
                        request.getRequestURI(),
                        HttpStatus.BAD_REQUEST
                ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError<String>> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        MessageType messageType = request.getRequestURI().endsWith("/api/v1/auth/login")
                ? MessageType.INVALID_CREDENTIALS
                : MessageType.AUTHENTICATION_REQUIRED;

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorFactory.create(
                        messageType.getCode(),
                        messageType.getMessage(),
                        request.getRequestURI(),
                        HttpStatus.UNAUTHORIZED
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError<String>> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiErrorFactory.create(
                        MessageType.ACCESS_DENIED.getCode(),
                        MessageType.ACCESS_DENIED.getMessage(),
                        request.getRequestURI(),
                        HttpStatus.FORBIDDEN
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError<String>> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorFactory.create(
                        MessageType.GENERAL_EXCEPTION.getCode(),
                        MessageType.GENERAL_EXCEPTION.getMessage(),
                        request.getRequestURI(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));
    }
}
