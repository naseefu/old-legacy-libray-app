package com.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.AuthRequestDto;
import com.library.dto.AuthResponseDto;
import com.library.dto.RegisterRequestDto;
import com.library.model.User;
import com.library.service.AuthService;
import com.library.util.ApiResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
             @RequestBody AuthRequestDto request) throws Exception {
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
             @RequestBody RegisterRequestDto request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully: " + user.getUsername()));
    }
}
