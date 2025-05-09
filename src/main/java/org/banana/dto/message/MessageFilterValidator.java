package org.banana.dto.message;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class MessageFilterValidator implements ConstraintValidator<MessageFilterValidation, MessageFilterDto> {

    @Override
    public boolean isValid(final MessageFilterDto requestDto, final ConstraintValidatorContext context) {
        return (requestDto.getCursorMessageId() != null && requestDto.getCursorDateTime() != null)
               || (requestDto.getCursorMessageId() == null && requestDto.getCursorDateTime() == null);
    }
}
