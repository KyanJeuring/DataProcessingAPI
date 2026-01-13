package com.dataprocessingapi.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/api/hello")
    public String hello(@RequestParam(value = "name", required = false) String name) {
        String person = (name == null || name.isBlank()) ? "stranger" : name.trim();
        return String.format("Hello %s — this message is from the api", person);
    }
}
