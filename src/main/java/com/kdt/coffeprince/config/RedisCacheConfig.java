package com.kdt.coffeprince.config;

import com.kdt.coffeprince.dto.MenuResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;

/**
 * A06 전체 메뉴 조회 Redis 캐시 설정.
 * app.cache.enabled=true (기본값) 일 때 켜진다. local.env 에 CACHE_ENABLED=false 를 주면 꺼진다.
 * 꺼져 있으면 @EnableCaching 이 없어서 @Cacheable·@CacheEvict 가 무시되고 DB 에서 바로 조회한다.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true")
public class RedisCacheConfig {

    /** 메뉴 목록 캐시 이름. Redis 키는 "menus::all" 로 저장된다. */
    public static final String MENUS = "menus";

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            JsonMapper jsonMapper,
            @Value("${app.cache.menus-ttl-seconds}") long menusTtlSeconds
    ) {
        // List<MenuResponse> 를 사람이 읽을 수 있는 JSON 으로 저장 (redis-cli GET 으로 확인 가능)
        JavaType menuListType = jsonMapper.getTypeFactory()
                .constructCollectionType(List.class, MenuResponse.class);

        RedisCacheConfiguration menusConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(menusTtlSeconds))
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(jsonMapper, menuListType)));

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration(MENUS, menusConfig)
                .build();
    }
}
