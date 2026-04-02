package com.huseyinsacikay.exception;

import org.springframework.http.HttpStatus;

public final class ConflictException extends BaseException {
    public MessageType messageType;

    public ConflictException(MessageType messageType) {
        super(messageType.getMessage(), HttpStatus.CONFLICT);
        this.messageType = messageType;
    }
}
