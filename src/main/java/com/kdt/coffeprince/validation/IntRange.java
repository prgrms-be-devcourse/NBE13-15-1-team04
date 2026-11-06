package com.kdt.coffeprince.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


// URL 정수 공용 규칙: 부호 없는 10진 정수이면서 min ~ max 범위 안
// String 으로 받아 형식·범위를 한 번에 검사한다 (@Min 등은 "1.5" 를 통과시키므로 쓰지 않는다)
@Constraint(validatedBy = IntRangeValidator.class)
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
public @interface IntRange {
    int min() default 0;

    int max() default Integer.MAX_VALUE;

    String message() default "{com.kdt.coffeprince.validation.IntRange.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
