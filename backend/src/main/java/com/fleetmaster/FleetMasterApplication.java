package com.fleetmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "FleetMaster Data Processing API",
        version = "1.0",
        description = "API for managing fleet company accounts and data processing"
    )
)
public class FleetMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(FleetMasterApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
