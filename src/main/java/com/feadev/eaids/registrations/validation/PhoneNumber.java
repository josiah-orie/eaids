package com.feadev.eaids.registrations.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field-level constraint annotation that validates a phone number conforms
 * to E.164 international format (leading '+' followed by 7-15 digits,
 * with the first digit after '+' being non-zero).
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {

    String message() default "Phone number must be in E.164 format (e.g., +2348012345678)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
