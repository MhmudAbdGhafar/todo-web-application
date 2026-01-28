package org.example.user_web_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordPolicyValidator implements ConstraintValidator<PasswordPolicy, String> {

    // 8-64, at least: 1 lower, 1 upper, 1 digit, 1 special
    private static final String STRONG_PASSWORD =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,64}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null){
            return false;
        }

        return value.matches(STRONG_PASSWORD);
    }
}