package org.example.mall_tiny01.config;

import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 客户端全局配置
 *
 * 为所有 Feign 服务间调用自动添加 JWT token，
 * 解决微服务内部调用被 JwtInterceptor 拦截导致的 401 "未登录" 问题。
 *
 * 原理：
 *   每次 Feign 发起请求前，自动生成一个 service 级别的 JWT token 放入请求头，
 *   目标服务的 JwtInterceptor 验证通过后放行。
 *
 * @ConditionalOnClass 确保只在有 Feign 的模块中生效，不影响 mall-gateway 等模块。
 */
@Configuration
@ConditionalOnClass(RequestInterceptor.class)
public class FeignConfig {

    @Bean
    public RequestInterceptor jwtFeignInterceptor(JwtConfig jwtConfig) {
        return template -> {
            String serviceToken = jwtConfig.getToken("internal-service");
            template.header("token", serviceToken);
        };
    }
}