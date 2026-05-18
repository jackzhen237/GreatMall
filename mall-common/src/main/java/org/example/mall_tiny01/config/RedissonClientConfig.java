package org.example.mall_tiny01.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 客户端配置 —— 从 spring.data.redis 读连接信息构建 RedissonClient。
 *
 * 为什么不用 yml 里的 spring.redis 前缀？
 * spring.redis.host 不是标准 Spring Boot 属性，IDEA 的 YAML 校验器不认识会报红。
 * 这里直接从 Spring Boot 标准的 spring.data.redis.* 属性读取，IDEA 不报错，
 * Redisson 也正常工作。
 */
@Configuration
public class RedissonClientConfig {

    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort())
                .setDatabase(redisProperties.getDatabase());
        return Redisson.create(config);
    }
}
