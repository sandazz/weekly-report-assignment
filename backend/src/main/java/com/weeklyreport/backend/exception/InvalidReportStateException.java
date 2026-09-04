package com.weeklyreport.backend.exception;

public class InvalidReportStateException extends RuntimeException {

    public InvalidReportStateException(String message) {
        super(message);
    }
}
