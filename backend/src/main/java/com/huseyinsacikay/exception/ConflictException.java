package com.huseyinsacikay.exception;

import org.springframework.http.HttpStatus;

public final class ConflictException extends BaseException {
    public ConflictException(MessageType messageType) {
        super(messageType, HttpStatus.CONFLICT);
    }
}
