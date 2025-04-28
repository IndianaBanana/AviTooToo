package org.banana.security.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.banana.dto.user.UserRegisterRequestDto;


public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, UserRegisterRequestDto> {

    @Override
    public boolean isValid(final UserRegisterRequestDto user, final ConstraintValidatorContext context) {
        return user.getPassword().equals(user.getMatchingPassword());
    }

}
