package com.bloomshield.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bloomshield.model.User;
import com.bloomshield.service.LookupService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


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
    public ResponseEntity<Map<String, Object>> registerUser(@RequestParam("userName") String userName) {
        boolean success = lookupService.registerUser(userName);
        if (!success) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "user already exists"));
        }
        return ResponseEntity.ok(Map.of("status", "user-created"));
    }

    @GetMapping("/user/{userName}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable("userName") String userName) {
        long startTime = System.nanoTime();
        boolean success = lookupService.checkIfUserExits(userName);
        if (!success) {
            long endTime = System.nanoTime();
            long timeElapsed = endTime - startTime;
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "user not found", "time_elapsed", timeElapsed));
        }
        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        return ResponseEntity.ok(Map.of("status", "user found", "time_elapsed", timeElapsed));
    }

    @GetMapping("/list-users")
    public List<User> userlist() {
        return lookupService.listUsers();
    }
}
