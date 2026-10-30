package com.hostelmanagement.exception;

public class HostelException extends RuntimeException {
    public HostelException(String message) {
        super(message);
    }
    public HostelException(String message, Throwable cause) {
        super(message, cause);
    }
}
