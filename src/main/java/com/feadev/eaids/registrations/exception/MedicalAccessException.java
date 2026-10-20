package com.feadev.eaids.registrations.exception;

/**
 * Thrown when a user attempts to access or modify a medical resource they are not
 * authorised to access. Maps to HTTP 403 Forbidden.
 */
public class MedicalAccessException extends RuntimeException {

    public MedicalAccessException(String message) {
        super(message);
    }
}
