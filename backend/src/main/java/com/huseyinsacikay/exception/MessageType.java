package com.huseyinsacikay.exception;

import lombok.Getter;

@Getter
public enum MessageType {
    NO_RECORD_EXIST("1001", "Record not found"),
    USER_ALREADY_EXISTS("1002", "User already exists"),
    PITCH_NOT_AVAILABLE("1003", "Pitch is not available for selected time"),
    RESERVATION_TIME_IN_PAST("1004", "Reservation start time must be in the future"),
    INVALID_RESERVATION_TIME_RANGE("1005", "Reservation end time must be after start time"),
    RESERVATION_TIME_OVERLAP("1006", "Pitch already has an active reservation for the selected time"),
    ACCESS_DENIED("1007", "You are not allowed to access this reservation"),
    RESERVATION_NOT_CANCELLABLE("1008", "Reservation cannot be cancelled in its current state"),
    VALIDATION_ERROR("1009", "Request validation failed"),
    AUTHENTICATION_REQUIRED("1010", "Authentication is required to access this resource"),
    INVALID_CREDENTIALS("1011", "Invalid username or password"),
    GENERAL_EXCEPTION("9999", "An unexpected error occurred");

    private final String code;
    private final String message;

    MessageType(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
