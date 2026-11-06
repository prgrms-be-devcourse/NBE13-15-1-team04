package com.kdt.coffeprince.config;

import com.kdt.coffeprince.util.DateUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {
    @Bean
    public Clock clock() {
        return Clock.system(DateUtils.ZONE);
    }
}
