package org.example.user_web_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = Otp6DigitsValidator.class)
public @interface Otp6Digits {
    String message() default "OTP must be exactly 6 digits";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}