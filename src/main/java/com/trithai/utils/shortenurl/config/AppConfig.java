package com.trithai.utils.shortenurl.config;

import lombok.Getter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AppConfig {
    private final String shortenDomain;
    private final Long machineId;
    private final String redisHost;
    private final String redisPort;

    public AppConfig(
            @Value("${app.shorten-domain}") String shortenDomain,
            @Value("${app.machine-id}") Long machineId,
            @Value("${spring.data.redis.host}") String redisHost,
            @Value("${spring.data.redis.port}") String redisPort) {
        this.shortenDomain = shortenDomain;
        this.machineId = machineId;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        return template;
    }

    @Bean
    public Config getConfig() {
        Config config = new Config();
        String redis = "redis://" + redisHost + ":" + redisPort;
        config.useSingleServer().setAddress(redis);
        return config;
    }

    @Bean
    public RedissonClient getRedisClient(Config config) {
        return Redisson.create(config);
    }

}
