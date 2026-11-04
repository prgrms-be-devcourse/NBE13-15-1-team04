package com.kdt.coffeprince.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// @CreatedDate / @LastModifiedDate 활성화. 메인 클래스와 분리해 슬라이스 테스트에 영향 없도록 함
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
