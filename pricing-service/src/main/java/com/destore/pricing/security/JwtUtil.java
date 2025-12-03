package com.destore.pricing.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * @file JwtUtil.java
 * @brief Utility class for JWT token validation
 * 
 * Validates JWT tokens and extracts claims (username, role, storeId)
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    /**
     * @brief Get the signing key for JWT operations
     * @return SecretKey for validating JWTs
     */
    private SecretKey getSigningKey() {
        logger.info("Using JWT secret starting with: {}...", secret.substring(0, Math.min(10, secret.length())));
        logger.info("JWT secret length: {}", secret.length());
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * @brief Extract username from JWT token
     * @param token JWT token
     * @return Username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * @brief Extract role from JWT token
     * @param token JWT token
     * @return User role (NETWORK_MANAGER or STORE_MANAGER)
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * @brief Extract store ID from JWT token
     * @param token JWT token
     * @return Store ID (null for network managers)
     */
    public Integer extractStoreId(String token) {
        return extractClaim(token, claims -> claims.get("storeId", Integer.class));
    }

    /**
     * @brief Extract expiration date from JWT token
     * @param token JWT token
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * @brief Extract a specific claim from JWT token
     * @param token JWT token
     * @param claimsResolver Function to extract the claim
     * @param <T> Type of the claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * @brief Extract all claims from JWT token
     * @param token JWT token
     * @return All claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * @brief Check if token is expired
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * @brief Validate JWT token
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
