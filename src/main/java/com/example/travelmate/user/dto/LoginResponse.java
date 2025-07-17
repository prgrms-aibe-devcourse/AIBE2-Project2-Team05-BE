package com.example.travelmate.user.dto;

import com.example.travelmate.user.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String email;
    private Role role;
}