package com.sps.auth.controller;

import com.sps.auth.dto.*;
import com.sps.auth.entity.UserEntity;
import com.sps.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            UserEntity user = authService.register(request);
            RegisterResponse response = new RegisterResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    "User registered successfully"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = authService.login(request);
            UserEntity user = authService.findByUsername(request.getUsername());
            LoginResponse response = new LoginResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getCedula());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/validate-cedula")
    public ResponseEntity<?> validateCedula(@Valid @RequestBody ValidateCedulaRequest request) {
        try {
            UserEntity user = authService.validateCedulaAndPassword(request.getCedula(), request.getPassword());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody ValidateRequest request) {
        boolean valid = authService.validateToken(request.getToken());
        ValidateResponse response = new ValidateResponse();
        response.setValid(valid);
        if (valid) {
            response.setUsername(authService.extractUsername(request.getToken()));
        }
        return ResponseEntity.ok(response);
    }
}