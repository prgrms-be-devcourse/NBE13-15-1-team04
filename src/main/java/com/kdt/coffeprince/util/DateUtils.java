package com.kdt.coffeprince.util;

import java.time.LocalDate;
import java.time.ZoneId;

public class DateUtils {
    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private DateUtils() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static LocalDate getOrDefault(LocalDate date) {
        return (date == null) ? today() : date;
    }

    /**
     * 내일 이후의 날짜인지 반환한다. 오늘은 false.
     */
    public static boolean isFuture(LocalDate date) {
        return date.isAfter(today());
    }
}
