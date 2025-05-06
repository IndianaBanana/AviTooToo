package org.banana.security.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.banana.security.dto.UserRegisterRequestDto;


public class PasswordOnRegisterValidator
        implements ConstraintValidator<PasswordOnRegisterValidation, UserRegisterRequestDto> {

    @Override
    public boolean isValid(final UserRegisterRequestDto user, final ConstraintValidatorContext context) {
        return user.getPassword().equals(user.getMatchingPassword());
//        if (!user.getPassword().equals(user.getMatchingPassword())) {
//            context.buildConstraintViolationWithTemplate("Password and matching password must be equal")
//                    .addPropertyNode("matchingPassword")
//                    .addConstraintViolation();
//            return false;
//        }
//        return true;
    }
}
