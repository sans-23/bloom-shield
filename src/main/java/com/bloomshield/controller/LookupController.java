package com.bloomshield.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bloomshield.service.LookupService;

import org.springframework.web.bind.annotation.GetMapping;


import java.util.Map;

@RestController
@RequestMapping("/api")
public class LookupController {

    private final LookupService lookupService;

    public LookupController(LookupService lookupService){
        this.lookupService = lookupService;
    }
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "up"
        );
    }

    @GetMapping("/register-user")
    public Map<String, Object> registerUser(String user_name) {
        lookupService.registerUser(user_name);
        return Map.of(
            "status", "user-created"
        );
    }
    
}
