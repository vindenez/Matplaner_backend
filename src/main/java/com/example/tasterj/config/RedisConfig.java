package com.example.tasterj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // Extract host, port, password from the REDIS_URL environment variable
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        String redisUrl = System.getenv("REDIS_URL");

        // Assuming the format is redis://:password@hostname:port
        String urlWithoutProtocol = redisUrl.substring(8); // remove 'redis://'
        String[] urlParts = urlWithoutProtocol.split("@");
        String[] authParts = urlParts[0].split(":");
        String[] hostParts = urlParts[1].split(":");

        redisConfig.setPassword(authParts[1]);
        redisConfig.setHostName(hostParts[0]);
        redisConfig.setPort(Integer.parseInt(hostParts[1]));

        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
