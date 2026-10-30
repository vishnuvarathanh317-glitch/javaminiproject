package com.hostelmanagement.exception;

public class DatabaseException extends HostelException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    public DatabaseException(String message) {
        super(message);
    }
}
