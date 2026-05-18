package org.example.mall_tiny01.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("org.example.mall_tiny01.mbg.mapper")
public class MapperConfig {
}
