package com.kdt.coffeprince.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

// 운영의 Clock 빈(ClockConfig) 대신 시각을 바꿀 수 있는 MutableClock 을 주입한다.
// 사용: 테스트 클래스에 @Import(TestClockConfig.class)
@TestConfiguration
public class TestClockConfig {

    @Bean
    @Primary
    public MutableClock testClock() {
        return new MutableClock();
    }
}
