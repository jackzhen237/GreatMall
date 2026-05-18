package org.example.mall_tiny01.config;

import lombok.extern.slf4j.Slf4j;
import org.example.mall_tiny01.component.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    // 1. 配置跨域（解决 403 Forbidden 问题）
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许所有方法
                .allowedHeaders("*")         // 允许所有请求头
                .allowCredentials(true);     // 允许携带 Cookie
    }

    // 2. 配置拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("正在注册拦截器，放行路径: /admin/login, /login,/sso/login");
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/login", "/login","/sso/login");
    }
}
