package com.kdt.coffeprince.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

class UrlDateValidator implements ConstraintValidator<UrlDate, String> {

    private static final Pattern FORMAT = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    public static final LocalDate MIN = LocalDate.of(2000, 1, 1);
    public static final LocalDate MAX = LocalDate.of(2099, 12, 31);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null) return true;
        if (!FORMAT.matcher(value).matches()) return false;
        LocalDate date;
        try {
            date = LocalDate.parse(value);                    // ISO_LOCAL_DATE 는 STRICT: 2026-02-30 거부
        } catch (DateTimeParseException e) {
            return false;
        }
        return !date.isBefore(MIN) && !date.isAfter(MAX);
    }
}