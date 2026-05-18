package org.example;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"org.example"})
@MapperScan("org.example.mall_tiny01.mbg.mapper")
public class cmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(cmsApplication.class, args);
    }

}