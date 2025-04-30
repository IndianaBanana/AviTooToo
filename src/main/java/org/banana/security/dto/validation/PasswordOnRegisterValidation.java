package org.banana.security.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = PasswordOnRegisterValidator.class)
@Documented
public @interface PasswordOnRegisterValidation {

    String message() default "Password and matching password must be equal";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
