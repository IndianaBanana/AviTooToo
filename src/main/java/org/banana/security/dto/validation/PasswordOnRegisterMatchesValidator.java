package org.banana.security.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.banana.security.dto.UserRegisterRequestDto;


public class PasswordOnRegisterMatchesValidator implements ConstraintValidator<PasswordOnRegisterMatches, UserRegisterRequestDto> {

    @Override
    public boolean isValid(final UserRegisterRequestDto user, final ConstraintValidatorContext context) {
        return user.getPassword().equals(user.getMatchingPassword());
    }
}
