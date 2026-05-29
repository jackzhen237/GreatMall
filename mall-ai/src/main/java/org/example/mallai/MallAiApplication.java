package org.example.mallai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"org.example"})
@EnableFeignClients(basePackages = {"org.example.mall_tiny01.feign"})
public class MallAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallAiApplication.class, args);
    }

}
