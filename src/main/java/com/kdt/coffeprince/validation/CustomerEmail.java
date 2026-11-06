package com.kdt.coffeprince.validation;

import com.kdt.coffeprince.constant.Regexp;
import com.kdt.coffeprince.entity.Order;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

// 고객 이메일: 필수, 명세 정규식, 100자 이하 (A11 본문·A12 쿼리 공용)
// 조합 제약이라 위반 시 각 제약(@NotBlank·@Pattern·@Size)의 문구가 그대로 나온다
@NotBlank
@Pattern(regexp = Regexp.EMAIL)
@Size(max = Order.MAX_EMAIL_LENGTH)
@Constraint(validatedBy = {})
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Retention(RUNTIME)
@Documented
public @interface CustomerEmail {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}