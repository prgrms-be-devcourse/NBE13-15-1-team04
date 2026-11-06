package com.kdt.coffeprince.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


// URL 날짜 공용 규칙: yyyy-MM-dd 형식의 2000-01-01 ~ 2099-12-31 사이 실제 날짜
// String 으로 받아 형식·실제 날짜·범위를 한 번에 검사한다
@Constraint(validatedBy = UrlDateValidator.class)
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
public @interface UrlDate {
    String message() default "{com.kdt.coffeprince.validation.UrlDate.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}


