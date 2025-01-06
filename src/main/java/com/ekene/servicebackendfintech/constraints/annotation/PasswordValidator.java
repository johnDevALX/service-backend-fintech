package com.ekene.servicebackendfintech.constraints.annotation;

import com.ekene.servicebackendfintech.constraints.service.PasswordValidatorImpl;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordValidatorImpl.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordValidator {
    String message() default "Password must contain at least 8 characters, including uppercase, lowercase, and a number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

