package com.feadev.eaids.registrations.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * Validator for {@link PasswordMatch} annotation.
 * Uses BeanWrapper to dynamically access "password" and "confirmPassword" fields
 * from the annotated class.
 */
public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(value);

        Object password = beanWrapper.getPropertyValue("password");
        Object confirmPassword = beanWrapper.getPropertyValue("confirmPassword");

        if (password == null && confirmPassword == null) {
            return true;
        }

        if (password == null || confirmPassword == null) {
            return false;
        }

        return password.equals(confirmPassword);
    }
}
