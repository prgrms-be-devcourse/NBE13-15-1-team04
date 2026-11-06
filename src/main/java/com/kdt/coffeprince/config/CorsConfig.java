package com.kdt.coffeprince.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//깃 수정
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // 명세: 제공 web 화면 http://127.0.0.1:8000 (localhost 와 다른 Origin 이라 127.0.0.1 로 통일) + 배포 화면
                .allowedOrigins("http://127.0.0.1:8000", "https://team04.be15-algo-study.xyz")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("content-type")
                .maxAge(3600);
    }
}
