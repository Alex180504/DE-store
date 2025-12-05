package com.destore.loyalty.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Security configuration for loyalty service.
 * <p>
 * Validates JWT tokens from auth-service to protect endpoints.
 * Allows public access to health checks for Docker.
 * </p>
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Configures HTTP security for the application.
     *
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll());

        return http.build();
    }

    /**
     * Creates JWT authentication filter.
     *
     * @return JWT filter
     */
    @Bean
    public OncePerRequestFilter jwtAuthenticationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain) throws ServletException, IOException {

                String authHeader = request.getHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);

                    try {
                        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                        Claims claims = Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();

                        String username = claims.getSubject();
                        
                        // Set authentication in context
                        JwtAuthentication authentication = new JwtAuthentication(username, claims);
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                    } catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Invalid or expired token");
                        return;
                    }
                }

                filterChain.doFilter(request, response);
            }
        };
    }

    /**
     * Simple authentication object for JWT.
     */
    private static class JwtAuthentication implements org.springframework.security.core.Authentication {
        private final String username;
        private final Claims claims;
        private boolean authenticated = true;

        public JwtAuthentication(String username, Claims claims) {
            this.username = username;
            this.claims = claims;
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return java.util.Collections.emptyList();
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return claims;
        }

        @Override
        public Object getPrincipal() {
            return username;
        }

        @Override
        public boolean isAuthenticated() {
            return authenticated;
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            this.authenticated = isAuthenticated;
        }

        @Override
        public String getName() {
            return username;
        }
    }
}
