package com.fulfillx.auth.service;

import com.fulfillx.auth.dto.*;
import com.fulfillx.auth.entity.User;
import com.fulfillx.auth.exception.EmailAlreadyExistsException;
import com.fulfillx.auth.exception.InvalidTokenException;
import com.fulfillx.auth.repository.UserRepository;
import com.fulfillx.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
            throw new EmailAlreadyExistsException(request.getEmail() + " is already been Registered.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .tenantId(request.getTenantId())
                .active(true)
                .build();

        user = userRepository.save(user);

        return AuthResponse.builder()
                .email(user.getEmail())
                .role(user.getRole().name())
                .tenantId(user.getTenantId())
                .build();
    }

    // Login
    public AuthResponse login(LoginRequest request) {
        System.out.println("Execution start");
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        System.out.println("executed upper");
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("executed check");
        // Login
        return AuthResponse.builder()
                .accessToken(jwtUtil.generateToken(user.getEmail(), user.getRole().toString(), user.getTenantId(), user.getId()))
                .refreshToken(jwtUtil.generateRefreshToken(user.getEmail()))
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
                .accessToken(jwtUtil.generateToken(user.getEmail(), user.getRole().toString(), user.getTenantId(), user.getId()))
                .refreshToken(jwtUtil.generateRefreshToken(user.getEmail()))
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