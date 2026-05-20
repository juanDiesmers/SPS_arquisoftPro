package com.sps.auth.controller;

import com.sps.auth.dto.LoginRequest;
import com.sps.auth.dto.LoginResponse;
import com.sps.auth.dto.ValidateRequest;
import com.sps.auth.dto.ValidateResponse;
import com.sps.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(@Valid @RequestBody ValidateRequest request) {
        boolean valid = authService.validateToken(request.getToken());
        return ResponseEntity.status(valid ? HttpStatus.OK : HttpStatus.UNAUTHORIZED)
                .body(new ValidateResponse(valid));
    }
}
