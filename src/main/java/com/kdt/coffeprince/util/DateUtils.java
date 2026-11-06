package com.kdt.coffeprince.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateUtils {
    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private DateUtils() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static Instant startOf(LocalDate date) {
        return date.atStartOfDay(ZONE).toInstant();
    }

    public static Instant startOfNextDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay(ZONE).toInstant();
    }
}
