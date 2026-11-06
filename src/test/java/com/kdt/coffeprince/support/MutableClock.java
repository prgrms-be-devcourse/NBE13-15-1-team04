package com.kdt.coffeprince.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

// 테스트에서 "지금 시각"을 마음대로 바꾸기 위한 Clock (서울 시각 기준으로 설정)
public class MutableClock extends Clock {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private volatile Instant now = Instant.now();

    // year: 년, mon: 월, day: 일, hour: 시, min: 분, sec: 초 (모두 서울 시각)
    public void set(int year, int mon, int day, int hour, int min, int sec) {
        this.now = ZonedDateTime.of(year, mon, day, hour, min, sec, 0, SEOUL).toInstant();
    }

    @Override
    public ZoneId getZone() {
        return SEOUL;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return now;
    }
}
