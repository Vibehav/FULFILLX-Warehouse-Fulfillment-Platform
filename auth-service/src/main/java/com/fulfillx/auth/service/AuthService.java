package com.fulfillx.auth.service;

import com.fulfillx.auth.dto.*;
import com.fulfillx.auth.entity.User;
import com.fulfillx.auth.exception.EmailAlreadyExistsException;
import com.fulfillx.auth.exception.InvalidTokenException;
import com.fulfillx.auth.repository.UserRepository;
import com.fulfillx.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // Register
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .tenantId(request.getTenantId())
                .active(true)
                .build();

        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateToken(user))
                .refreshToken(jwtUtil.generateRefreshToken(user))
                .email(user.getEmail())
                .role(user.getRole().name())
                .tenantId(user.getTenantId())
                .build();
    }

    // Login
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateToken(user))
                .refreshToken(jwtUtil.generateRefreshToken(user))
                .email(user.getEmail())
                .role(user.getRole().name())
                .tenantId(user.getTenantId())
                .build();
    }

    // Refresh Token
    public AuthResponse refresh(RefreshTokenRequest request) {

        String token = request.getRefreshToken();

        if (!jwtUtil.isTokenValid(token)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateToken(user))
                .refreshToken(jwtUtil.generateRefreshToken(user))
                .email(user.getEmail())
                .role(user.getRole().name())
                .tenantId(user.getTenantId())
                .build();
    }

    // Logout
    public void logout(String token) {
        jwtUtil.blacklistToken(token);
    }
}