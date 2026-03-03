package com.project.realrank.config.redis;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        RecordSupportingTypeResolver typeResolver = new RecordSupportingTypeResolver(ObjectMapper.DefaultTyping.NON_FINAL, objectMapper.getPolymorphicTypeValidator());
        StdTypeResolverBuilder initializedResolver = typeResolver.init(JsonTypeInfo.Id.CLASS, null);
        initializedResolver = initializedResolver.inclusion(JsonTypeInfo.As.PROPERTY);
        objectMapper.setDefaultTyping(initializedResolver);

        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        RedisCacheConfiguration redisCacheConfiguration =
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        .serializeKeysWith( // Redis 에 Key 를 저장할 때 String 으로 직렬화해 저장
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new StringRedisSerializer()
                                ))
                        .serializeValuesWith(   // Redis에 value를 저장할때 DTO 으로 직렬화 해 저장
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        valueSerializer
                                )
                        )
                        .entryTtl(Duration.ofMinutes(1)); // TTL 설정
        return RedisCacheManager
                .RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }


}
