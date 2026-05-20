package com.fulfillx.common.security;

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

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;


    // Generate Access Token
    public String generateToken(String email,String role,String tenantId,String userId) {

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("tenantId", tenantId)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // Generate Refresh Token
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUserId(String token) {
        return getClaims(token).get("userId", String.class);
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


    public void blacklistToken(String token) {
        // Extract expiration date from the token claims
        Claims claims = getClaims(token);
        Date expirationDate = claims.getExpiration();

        // calculate the remaining time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();
        long expirationTimeMillis = expirationDate.getTime();
        long ttl = expirationTimeMillis - currentTimeMillis;

        // store in redis only if the token hasn't expired yet
        if (ttl > 0) {
            String key = "blacklist:" + token;
            redisTemplate.opsForValue().set(key, "true", ttl, TimeUnit.MILLISECONDS);
        }
    }

    // Checks if a specific token exists in the Redis blacklist.
    public boolean isTokenBlacklisted(String token) {
        String key = "blacklist:" + token;
        Boolean hasKey = redisTemplate.hasKey(key);
        return hasKey;
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