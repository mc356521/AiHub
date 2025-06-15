package com.example.aihub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;

@MapperScan("com.example.aihub.mapper")
@SpringBootApplication(exclude = {FreeMarkerAutoConfiguration.class})
public class AiHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiHubApplication.class, args);
    }

}
