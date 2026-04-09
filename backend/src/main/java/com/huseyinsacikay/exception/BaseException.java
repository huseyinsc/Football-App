package com.huseyinsacikay.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract sealed class BaseException extends RuntimeException permits NotFoundException, ConflictException, BadRequestException {
    private final HttpStatus status;
    private final MessageType messageType;

    public BaseException(MessageType messageType, HttpStatus status) {
        super(messageType.getMessage());
        this.status = status;
        this.messageType = messageType;
    }
}

