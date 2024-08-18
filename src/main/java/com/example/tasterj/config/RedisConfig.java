package com.example.tasterj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class RedisConfig {

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() throws URISyntaxException {
        String redisUrl = System.getenv("REDIS_URL");

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();

        if (redisUrl != null) {
            URI redisUri = new URI(redisUrl);

            redisStandaloneConfiguration.setHostName(redisUri.getHost());
            redisStandaloneConfiguration.setPort(redisUri.getPort());

            String userInfo = redisUri.getUserInfo();
            if (userInfo != null && userInfo.contains(":")) {
                redisStandaloneConfiguration.setPassword(userInfo.split(":", 2)[1]);
            }
        } else {
            // Fallback for local development if needed
            redisStandaloneConfiguration.setHostName("localhost");
            redisStandaloneConfiguration.setPort(6379);
        }

        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() throws URISyntaxException {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }
}