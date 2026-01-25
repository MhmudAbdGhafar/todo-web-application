package org.example.user_web_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;
import java.util.Objects;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        if (value == null) return true;

        try {
            Method passwordMethod =
                    value.getClass().getMethod("password");
            Method confirmPasswordMethod =
                    value.getClass().getMethod("confirmPassword");

            Object password = passwordMethod.invoke(value);
            Object confirmPassword = confirmPasswordMethod.invoke(value);

            return Objects.equals(password, confirmPassword);

        } catch (NoSuchMethodException e) {
            // DTO does not support password matching → ignore
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
