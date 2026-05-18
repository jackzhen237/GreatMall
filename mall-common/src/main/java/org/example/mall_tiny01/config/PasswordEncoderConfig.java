package org.example.mall_tiny01.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密配置 —— 提供 BCryptPasswordEncoder Bean 给所有模块共用。
 * Admin 登录和 Member 登录都用这个 Bean 做密码比对和加密。
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
