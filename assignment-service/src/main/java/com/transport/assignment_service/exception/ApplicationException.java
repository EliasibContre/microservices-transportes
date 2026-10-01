package com.transport.assignment_service.exception;
public class ApplicationException extends RuntimeException {

    public enum Reason {
        RESOURCE_NOT_FOUND,
        BUSINESS_RULE,
        INVALID_FILE
    }

    private final Reason reason;

    public ApplicationException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}