package com.shevtsov.sunshine.exceptions;

public class AuthenticationCredentialsReadingException extends RuntimeException {
    public AuthenticationCredentialsReadingException() {
        super("Credentials are incorrect or your JWT token expired");
    }
}
