package org.example.user_web_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;

public class AtLeastOneFieldValidator implements ConstraintValidator<AtLeastOneField, Object> {

    private String[] fields;

    @Override
    public void initialize(AtLeastOneField constraintAnnotation) {
        this.fields = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        if (value == null){
            return false;
        }

        try {
            for (String field : fields) {

                Method accessor = value.getClass().getMethod(field);
                Object v = accessor.invoke(value);

                if (v != null){
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            return true; // don’t block runtime if reflection fails
        }
    }
}