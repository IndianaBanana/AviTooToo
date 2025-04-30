package org.banana.security.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.banana.security.dto.UserPasswordUpdateRequestDto;


public class PasswordChangeValidator implements ConstraintValidator<PasswordChangeValidation, UserPasswordUpdateRequestDto> {

    @Override
    public boolean isValid(final UserPasswordUpdateRequestDto dto, final ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            context.buildConstraintViolationWithTemplate("New password must be different from old password")
                    .addPropertyNode("newPassword")
                    .addConstraintViolation();
            return false;
        }

        if (!dto.getNewPassword().equals(dto.getMatchingNewPassword())) {
            context.buildConstraintViolationWithTemplate("New password and matching password must be equal")
                    .addPropertyNode("matchingNewPassword")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
