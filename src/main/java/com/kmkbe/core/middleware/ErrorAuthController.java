package com.kmkbe.core.middleware;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/error")
public class ErrorAuthController {

    @GetMapping("/unauthorized")
    public ResponseEntity<Map<String, String>> handleUnauthorized() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "401");
        response.put("error", "Unauthorized");
        response.put("scope", "");
        response.put("message", "You are not authorized to access this resource");
        return ResponseEntity.status(401).body(response);
    }

    @GetMapping("/internal/unauthorized")
    public ResponseEntity<Map<String, String>> handleInternalUnauthorized() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "401");
        response.put("error", "Unauthorized");
        response.put("scope", "internal");
        response.put("message", "You are not authorized to access this resource");
        return ResponseEntity.status(401).body(response);
    }
}