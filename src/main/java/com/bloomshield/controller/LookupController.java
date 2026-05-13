package com.bloomshield.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


import java.util.Map;

@RestController
@RequestMapping("/api")
public class LookupController {
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "up"
        );
    }
    
}
