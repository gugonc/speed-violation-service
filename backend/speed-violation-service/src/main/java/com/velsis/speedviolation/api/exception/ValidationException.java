package com.velsis.speedviolation.api.exception;

public class ValidationException extends RuntimeException {

    private final transient ApiError error;

    public ValidationException(ApiError error, String message) {
        super(message);
        this.error = error;
    }

    public ApiError error() {
        return error;
    }
}
