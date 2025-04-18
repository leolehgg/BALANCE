package com.wellbeing.deviceusage.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ValidationErrorResponse extends ErrorResponse {
    private List<String> errors;

    public ValidationErrorResponse(int status, String message, LocalDateTime timestamp, List<String> errors) {
        super(status, message, timestamp);
        this.errors = errors;
    }
}