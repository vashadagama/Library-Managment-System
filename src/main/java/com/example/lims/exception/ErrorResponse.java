package com.example.lims.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    private String message;
    private LocalDateTime timeStamp;

    public ErrorResponse(String message) {
        this.message = message;
        this.timeStamp = LocalDateTime.now();
    }

    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timeStamp; }
}