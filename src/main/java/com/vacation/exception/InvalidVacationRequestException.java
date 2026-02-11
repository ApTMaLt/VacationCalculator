package com.vacation.exception;

public class InvalidVacationRequestException  extends RuntimeException {
    public InvalidVacationRequestException(String message) {
        super(message);
    }
}