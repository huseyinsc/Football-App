package com.huseyinsacikay.exception;

import org.springframework.http.HttpStatus;

public final class BadRequestException extends BaseException {
    public BadRequestException(MessageType messageType) {
        super(messageType, HttpStatus.BAD_REQUEST);
    }
}
