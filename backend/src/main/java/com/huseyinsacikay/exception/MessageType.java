package com.huseyinsacikay.exception;

import lombok.Getter;

@Getter
public enum MessageType {
    NO_RECORD_EXIST("1001", "Record not found"),
    USER_ALREADY_EXISTS("1002", "User already exists"),
    PITCH_NOT_AVAILABLE("1003", "Pitch is not available for selected time"),
    GENERAL_EXCEPTION("9999", "An unexpected error occurred");

    private final String code;
    private final String message;

    MessageType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}