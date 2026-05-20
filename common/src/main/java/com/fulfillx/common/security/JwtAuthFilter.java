package com.fulfillx.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Get Authorization header
        String authHeader = request.getHeader("Authorization");

        // 2. Check if header exists and starts with Bearer
        if (authHeader == null || authHeader.startsWith("Bearer ") == false) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract token
        String token = authHeader.substring(7);
//
//        //3a. Checking redis if the token is present in redis or not
//        String key = "blacklist:" + token;
//
//        if(redisTemplate.hasKey(key)){
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.getWriter().write("Token is logged out. Login Again! (Redis lookup)");
//            return;
//        }

        // 4. Validate token
        if (!jwtUtil.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token is invalid or blacklisted");
            return;
        }

        // 5. Extract user details from token
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);
        String tenantId = jwtUtil.extractTenantId(token);
        String userId = jwtUtil.extractUserId(token);

        // 6. Set authentication in Security Context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 7. Store tenantId in request for downstream use
        request.setAttribute("tenantId", tenantId);
        request.setAttribute("userId",userId);
        request.setAttribute("role",role);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}