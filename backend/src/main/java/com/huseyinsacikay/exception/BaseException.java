package com.huseyinsacikay.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
// Sealed class: Sadece belirli sınıflar inherit edebilir
public abstract sealed class BaseException extends RuntimeException permits NotFoundException, ConflictException {
    private final HttpStatus status;

    public BaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}


