package com.kdt.coffeprince.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

class IntRangeValidator implements ConstraintValidator<IntRange, String> {

    private static final Pattern FORMAT = Pattern.compile("0|[1-9]\\d{0,8}");   // 앞자리 0 거절, int 범위 안에서만 파싱

    private int min;
    private int max;

    @Override
    public void initialize(IntRange constraint) {
        this.min = constraint.min();
        this.max = constraint.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null) return true;
        if (!FORMAT.matcher(value).matches()) return false;
        int number = Integer.parseInt(value);
        return number >= min && number <= max;
    }
}
