package com.feadev.eaids.registrations.exception;

public class MaxLimitReachedException extends RuntimeException {

    public MaxLimitReachedException(String message) {
        super(message);
    }
}
