package com.shevtsov.sunshine.exceptions;

public class AuthorizationErrorException extends RuntimeException {
    public AuthorizationErrorException(String message) {
        super("Authorization error occurred: " + message);
    }
}
