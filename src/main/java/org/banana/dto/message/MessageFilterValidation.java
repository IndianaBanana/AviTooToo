package org.banana.dto.message;

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
@Constraint(validatedBy = MessageFilterValidator.class)
@Documented
public @interface MessageFilterValidation {

    String message() default "cursorMessageId and cursorDateTime must be both provided or not provided at all";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
