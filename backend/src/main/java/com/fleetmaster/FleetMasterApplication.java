package com.fleetmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
    info = @Info(
        title = "FleetMaster Data Processing API",
        version = "1.0",
        description = "API for managing fleet company accounts and data processing"
    ),
    servers = {
        @Server(url = "http://localhost:8081", description = "Local Development Server"),
        @Server(url = "http://127.0.0.1:8081", description = "Local Development Server (IP)")
    }
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
