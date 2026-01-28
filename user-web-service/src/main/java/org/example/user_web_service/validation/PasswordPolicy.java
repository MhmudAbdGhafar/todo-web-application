package org.example.user_web_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = PasswordPolicyValidator.class)
public @interface PasswordPolicy {

    String message() default "Password must be 8-64 chars and include at least 1 uppercase," +
            " 1 lowercase, 1 digit, 1 special character";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}