package com.destore.inventory.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT Token Provider - validates JWT tokens from auth service.
 * <p>
 * This component is responsible for validating JWT tokens and extracting claims such as username and role.
 * </p>
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;

    /**
     * Constructs a new JwtTokenProvider with the specified secret key.
     *
     * @param secret the secret key used for signing JWT tokens
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates the JWT token and extracts its claims.
     *
     * @param token the JWT token to validate
     * @return the claims extracted from the token
     */
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts the username from the JWT token.
     *
     * @param token the JWT token
     * @return the username (subject) from the token
     */
    public String getUsernameFromToken(String token) {
        return validateToken(token).getSubject();
    }

    /**
     * Extracts the role from the JWT token.
     *
     * @param token the JWT token
     * @return the role claim from the token
     */
    public String getRoleFromToken(String token) {
        return validateToken(token).get("role", String.class);
    }
}
