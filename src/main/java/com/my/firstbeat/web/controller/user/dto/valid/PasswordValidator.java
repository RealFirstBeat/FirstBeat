package com.my.firstbeat.web.controller.user.dto.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {


    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        if(password == null){
            return true;
        }

        if(password.length() < 8 || password.length() > 12){
            return false;
        }

        return password.chars().allMatch(Character::isLetterOrDigit);
    }
}
