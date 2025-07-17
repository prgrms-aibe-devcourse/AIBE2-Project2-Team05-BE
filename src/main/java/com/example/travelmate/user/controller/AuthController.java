package com.example.travelmate.user.controller;

import com.example.travelmate.common.security.CustomUserDetails;
import com.example.travelmate.user.dto.LoginRequest;
import com.example.travelmate.user.dto.LoginResponse;
import com.example.travelmate.user.dto.SignupRequest;
import com.example.travelmate.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Valid SignupRequest request) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<String> getProfile(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok("인증된 사용자: " + userDetails.getUsername());
    }
}