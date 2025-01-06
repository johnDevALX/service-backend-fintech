package com.ekene.servicebackendfintech.constraints.service;

import com.ekene.servicebackendfintech.constraints.annotation.PhoneNumberValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneNumberValidatorImpl implements ConstraintValidator<PhoneNumberValidator, String> {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{10}$");

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }
}


