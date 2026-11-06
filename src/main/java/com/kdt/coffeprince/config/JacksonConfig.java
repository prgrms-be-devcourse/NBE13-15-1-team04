package com.kdt.coffeprince.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.type.LogicalType;

// 명세 "잘못된 자료형은 400": 숫자·boolean 을 문자열 필드(name·address·postcode 등)로 자동 변환하지 않는다.
// 문자열 → 숫자/boolean 변환은 application.yaml 의 allow-coercion-of-scalars=false 가,
// 소수 → 정수 변환은 accept-float-as-int=false 가 막는다. 변환 거부는 HttpMessageNotReadableException 으로
// ValidationExceptionHandler 가 받아 fieldErrors.<필드> 로 응답한다.
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer rejectScalarToStringCoercion() {
        return builder -> builder.withCoercionConfig(LogicalType.Textual, config -> config
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
    }
}
