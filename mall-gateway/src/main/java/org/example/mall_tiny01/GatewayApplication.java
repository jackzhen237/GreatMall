package org.example.mall_tiny01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Mall 网关入口（Spring Cloud Gateway + Nacos 服务发现）。
 *
 * 请求流程：
 * 浏览器 → nginx(80) → Gateway(8080) → Nacos 查服务地址 → 路由到目标微服务
 *
 * 路由规则见 application.yml 的 spring.cloud.gateway.routes 配置。
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
