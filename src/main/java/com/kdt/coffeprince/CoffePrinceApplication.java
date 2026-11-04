package com.kdt.coffeprince;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@OpenAPIDefinition(info = @Info(title = "Grids & Circle API docs", version = "v0.1", description = "Grids & Circle 원두 주문 시스템 API 문서입니다."))
@SpringBootApplication
public class CoffePrinceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffePrinceApplication.class, args);
    }

}
