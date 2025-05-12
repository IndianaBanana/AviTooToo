package org.banana.security.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.banana.security.dto.UserRegisterRequestDto;


public class PasswordOnRegisterValidator
        implements ConstraintValidator<PasswordOnRegisterValidation, UserRegisterRequestDto> {

    @Override
    public boolean isValid(final UserRegisterRequestDto user, final ConstraintValidatorContext context) {
        return user.getPassword().equals(user.getMatchingPassword());
    }
}
