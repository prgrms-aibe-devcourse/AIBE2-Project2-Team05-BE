package com.example.travelmate.user.service;

import com.example.travelmate.common.jwt.JwtTokenProvider;
import com.example.travelmate.user.dto.LoginRequest;
import com.example.travelmate.user.dto.LoginResponse;
import com.example.travelmate.user.dto.SignupRequest;
import com.example.travelmate.user.entity.User;
import com.example.travelmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(request.getRole())
                .build();

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("이메일 없음"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호 불일치");
        }

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getEmail(), user.getRole());
    }
}
