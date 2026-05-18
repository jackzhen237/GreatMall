package org.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"org.example"})
@EnableFeignClients(clients = {org.example.mall_tiny01.feign.PmsFeignClient.class, org.example.mall_tiny01.feign.UmsFeignClient.class})
@MapperScan("org.example.mall_tiny01.mbg.mapper")
public class OmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(OmsApplication.class, args);
    }

}