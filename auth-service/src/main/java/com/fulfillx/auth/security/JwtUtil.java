package com.fulfillx.auth.security;

import com.fulfillx.auth.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final RedisTemplate<String, String> redisTemplate;

    // Generate Access Token
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("tenantId", user.getTenantId())
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // Generate Refresh Token
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // Extract Email from Token
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Extract Role from Token
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // Extract TenantId from Token
    public String extractTenantId(String token) {
        return getClaims(token).get("tenantId", String.class);
    }

    // Validate Token
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return !isTokenBlacklisted(token);
        } catch (JwtException e) {
            return false;
        }
    }

    // Blacklist Token on Logout
    public void blacklistToken(String token) {
        long expirationTime = getClaims(token)
                .getExpiration()
                .getTime() - System.currentTimeMillis();
        redisTemplate.opsForValue()
                .set("blacklist:" + token, "true", expirationTime, TimeUnit.MILLISECONDS);
    }

    // Check if Token is Blacklisted
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("blacklist:" + token)
        );
    }

    // Get Claims from Token
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Get Signing Key
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}