package com.huseyinsacikay.exception;

import org.springframework.http.HttpStatus;

public final class NotFoundException extends BaseException {
    public MessageType messageType;

    public NotFoundException(MessageType messageType) {
        super(messageType.getMessage(), HttpStatus.NOT_FOUND);
        this.messageType = messageType;
    }
}
