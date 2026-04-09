package com.huseyinsacikay.handler;

import com.huseyinsacikay.exception.BaseException;
import com.huseyinsacikay.exception.MessageType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiError<String>> handleBaseException(BaseException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(createApiError(ex.getMessageType().getCode(), ex.getMessage(), request, ex.getStatus()));
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
                .body(createApiError(MessageType.VALIDATION_ERROR.getCode(), validationErrors, request, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError<String>> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(createApiError(MessageType.ACCESS_DENIED.getCode(), MessageType.ACCESS_DENIED.getMessage(), request, HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError<String>> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createApiError(
                        MessageType.GENERAL_EXCEPTION.getCode(),
                        MessageType.GENERAL_EXCEPTION.getMessage(),
                        request,
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));
    }

    private <E> ApiError<E> createApiError(String code, E message, HttpServletRequest request, HttpStatus status) {
        ApiError<E> apiError = new ApiError<>();
        apiError.setStatus(status.value());

        InternalException<E> internalException = new InternalException<>();
        internalException.setCreateTime(OffsetDateTime.now());
        internalException.setCode(code);
        internalException.setPath(request.getRequestURI());
        internalException.setMessage(message);

        apiError.setException(internalException);
        return apiError;
    }
}
