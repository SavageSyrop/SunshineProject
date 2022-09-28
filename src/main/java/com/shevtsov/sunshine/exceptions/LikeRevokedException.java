package com.shevtsov.sunshine.exceptions;

public class LikeRevokedException extends RuntimeException {
    public LikeRevokedException() {
        super("You have already liked this! Your like has been revoked!");
    }
}
