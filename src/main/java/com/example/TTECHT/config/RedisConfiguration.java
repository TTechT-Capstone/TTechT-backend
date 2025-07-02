package com.example.TTECHT.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Slf4j
public class RedisConfiguration {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        log.info("Creating Redis connection factory for {}:{}", redisHost, redisPort);

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setDatabase(redisDatabase);

        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig);
        factory.setValidateConnection(true);
        return factory;
    }

    @Bean(name = "redisTemplate")
    @Primary
    public RedisTemplate<String, String> redisTemplate(LettuceConnectionFactory connectionFactory) {
        log.info("Creating RedisTemplate bean");

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure serializers - use String for everything to avoid issues
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setDefaultSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        // Don't enable transaction support as it can cause issues with increment
        template.setEnableTransactionSupport(false);
        template.afterPropertiesSet();

        log.info("RedisTemplate bean created successfully");
        return template;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateRedisConnection() {
        try {
            RedisTemplate<String, String> template = redisTemplate(lettuceConnectionFactory());

            // Test basic connection
            template.getConnectionFactory().getConnection().ping();

            // Test increment operation specifically
            String testKey = "test:increment:" + System.currentTimeMillis();
            template.opsForValue().set(testKey, "0");
            String result1 = template.opsForValue().get(testKey);

            // Test increment
            template.opsForValue().increment(testKey);
            String result2 = template.opsForValue().get(testKey);

            // Clean up
            template.delete(testKey);

            log.info("✅ Redis connection and increment test successful: {} -> {}", result1, result2);
        } catch (Exception e) {
            log.error("❌ Redis connection validation failed", e);
        }
    }
}