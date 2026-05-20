package com.sps.sns.controller;

import com.sps.sns.dto.SnsValidationRequest;
import com.sps.sns.service.SnsMockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SnsMockController {

    private final SnsMockService snsMockService;

    public SnsMockController(SnsMockService snsMockService) {
        this.snsMockService = snsMockService;
    }

    @PostMapping("/sns/validar")
    public ResponseEntity<Map<String, Object>> validar(@Valid @RequestBody SnsValidationRequest request) {
        return ResponseEntity.ok(snsMockService.validate(request));
    }
}
