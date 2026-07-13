package com.feadev.eaids.controller;

import com.feadev.eaids.registrations.dto.response.ErrorResponse;
import com.feadev.eaids.registrations.exception.*;
import net.jqwik.api.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Property-based tests for consistent error response structure.
 *
 * Property 25: Consistent Error Response Structure
 * Validates: Requirements 16.2, 16.4, 16.5
 *
 * For any error scenario, verifies the response contains timestamp, status, and message fields.
 */
class ConsistentErrorResponsePropertyTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Property
    void resourceNotFoundResponseHasRequiredFields(
            @ForAll("errorMessages") String message
    ) {
        ResourceNotFoundException ex = new ResourceNotFoundException(message);
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);

        assertErrorResponseStructure(response, 404);
    }

    @Property
    void duplicateResourceResponseHasRequiredFields(
            @ForAll("errorMessages") String message
    ) {
        DuplicateResourceException ex = new DuplicateResourceException(message);
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateResource(ex);

        assertErrorResponseStructure(response, 409);
    }

    @Property
    void invalidTokenResponseHasRequiredFields(
            @ForAll("errorMessages") String message
    ) {
        InvalidTokenException ex = new InvalidTokenException(message);
        ResponseEntity<ErrorResponse> response = handler.handleInvalidToken(ex);

        assertErrorResponseStructure(response, 401);
    }

    @Property
    void accountNotActiveResponseHasRequiredFields(
            @ForAll("errorMessages") String message
    ) {
        AccountNotActiveException ex = new AccountNotActiveException(message);
        ResponseEntity<ErrorResponse> response = handler.handleAccountNotActive(ex);

        assertErrorResponseStructure(response, 403);
    }

    @Property
    void maxLimitReachedResponseHasRequiredFields(
            @ForAll("errorMessages") String message
    ) {
        MaxLimitReachedException ex = new MaxLimitReachedException(message);
        ResponseEntity<ErrorResponse> response = handler.handleMaxLimitReached(ex);

        assertErrorResponseStructure(response, 400);
    }

    @Property
    void otpExhaustedResponseHasRequiredFields(
            @ForAll("errorMessages") String message
    ) {
        OtpExhaustedException ex = new OtpExhaustedException(message);
        ResponseEntity<ErrorResponse> response = handler.handleOtpExhausted(ex);

        assertErrorResponseStructure(response, 429);
    }

    @Property
    void invalidStateTransitionResponseHasRequiredFields(
            @ForAll("errorMessages") String message
    ) {
        InvalidStateTransitionException ex = new InvalidStateTransitionException(message);
        ResponseEntity<ErrorResponse> response = handler.handleInvalidStateTransition(ex);

        assertErrorResponseStructure(response, 400);
    }

    @Property
    void genericExceptionResponseHasRequiredFields(
            @ForAll("errorMessages") String message
    ) {
        Exception ex = new RuntimeException(message);
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertErrorResponseStructure(response, 500);
    }

    @Example
    void validationErrorResponseHasRequiredFieldsAndErrors() {
        // Create a mock validation exception with field errors
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.addError(new FieldError("request", "email", "Email is invalid"));
        bindingResult.addError(new FieldError("request", "password", "Password too short"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(ex);

        ErrorResponse body = response.getBody();
        assert body != null : "Response body should not be null";
        assert body.getTimestamp() != null && !body.getTimestamp().isEmpty()
                : "Timestamp should be present";
        assert body.getStatus() == 400
                : "Status should be 400 for validation errors";
        assert body.getMessage() != null && !body.getMessage().isEmpty()
                : "Message should be present";
        assert body.getErrors() != null && !body.getErrors().isEmpty()
                : "Field errors list should be present for validation failures";
        assert body.getErrors().size() == 2
                : "Expected 2 field errors";
    }

    private void assertErrorResponseStructure(ResponseEntity<ErrorResponse> response, int expectedStatus) {
        assert response != null : "ResponseEntity should not be null";
        assert response.getBody() != null : "Response body should not be null";

        ErrorResponse body = response.getBody();

        // Verify timestamp is present and non-empty
        assert body.getTimestamp() != null && !body.getTimestamp().isEmpty()
                : "Timestamp field must be present and non-empty, got: " + body.getTimestamp();

        // Verify status matches expected HTTP status code
        assert body.getStatus() == expectedStatus
                : "Status field should be " + expectedStatus + " but got: " + body.getStatus();

        // Verify message is present and non-empty
        assert body.getMessage() != null && !body.getMessage().isEmpty()
                : "Message field must be present and non-empty, got: " + body.getMessage();

        // Verify HTTP status code of ResponseEntity matches
        assert response.getStatusCode().value() == expectedStatus
                : "HTTP status should be " + expectedStatus + " but got: " + response.getStatusCode().value();
    }

    @Provide
    Arbitrary<String> errorMessages() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars(' ', '.', ':', '-', '_')
                .ofMinLength(5)
                .ofMaxLength(100);
    }
}
