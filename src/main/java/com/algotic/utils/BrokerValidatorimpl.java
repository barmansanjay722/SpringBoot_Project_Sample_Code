package com.algotic.utils;

import com.algotic.constants.BrokerEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.EnumUtils;

public class BrokerValidatorimpl implements ConstraintValidator<BrokerValidator, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return EnumUtils.isValidEnum(BrokerEnum.class, value);
    }
}
