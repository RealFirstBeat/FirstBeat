package com.my.firstbeat.web.controller.user.dto.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
@Constraint(validatedBy = PasswordValidator.class)
public @interface ValidPassword {

    String message() default "영문자 숫자 조합 8~12자 사이로 입력해야 합니다";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
