package org.example.user_web_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class Otp6DigitsValidator implements ConstraintValidator<Otp6Digits, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null){
            return false;
        }

        return value.matches("^\\d{6}$");
    }
}