package com.example.TTECHT.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class PasswordResetConfig {

    @Bean
    public RedisTemplate<String, String> regisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new RedisTemplate<String, String>() {{
            setConnectionFactory(redisConnectionFactory);
            setKeySerializer(new StringRedisSerializer());
            setValueSerializer(new StringRedisSerializer());
            setHashKeySerializer(new StringRedisSerializer());
            setHashValueSerializer(new StringRedisSerializer());
        }};

    }
}
