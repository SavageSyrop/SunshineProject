package com.shevtsov.sunshine.exceptions;

public class ActionAlreadyCompletedException extends RuntimeException{
    public ActionAlreadyCompletedException(String message) {
        super(message);
    }
}
