package com.shevtsov.sunshine.exceptions;

public class SelfInteractionException extends RuntimeException{
    public SelfInteractionException(String message) {
        super(message);
    }
}
