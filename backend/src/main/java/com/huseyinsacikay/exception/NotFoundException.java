package com.huseyinsacikay.exception;

import org.springframework.http.HttpStatus;

public final class NotFoundException extends BaseException {
    public NotFoundException(MessageType messageType) {
        super(messageType, HttpStatus.NOT_FOUND);
    }
}
