package org.banana.dto.advertisement;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class AdvertisementUpdateValidator implements ConstraintValidator<AdvertisementUpdateValidation, AdvertisementUpdateRequestDto> {

    @Override
    public boolean isValid(final AdvertisementUpdateRequestDto requestDto, final ConstraintValidatorContext context) {
        return (requestDto.getTitle() != null && !requestDto.getTitle().isBlank()) ||
               (requestDto.getDescription() != null && !requestDto.getDescription().isBlank()) ||
               requestDto.getCityId() != null ||
               requestDto.getAdvertisementTypeId() != null ||
               requestDto.getPrice() != null ||
               requestDto.getQuantity() != null;
    }
}
