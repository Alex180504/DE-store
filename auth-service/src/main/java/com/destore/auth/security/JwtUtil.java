package com.destore.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @file JwtUtil.java
 * @brief Utility class for JWT token operations
 * 
 * Handles JWT token generation, validation, and claim extraction
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * @brief Get the signing key for JWT operations
     * @return SecretKey for signing JWTs
     */
    private SecretKey getSigningKey() {
        logger.info("Auth Service - Using JWT secret starting with: {}...", secret.substring(0, Math.min(10, secret.length())));
        logger.info("Auth Service - JWT secret length: {}", secret.length());
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
     * @return User role as string
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
     * @return Extracted claim
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
     * @return True if expired
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * @brief Generate JWT token for a user
     * @param username Username
     * @param role User role
     * @param storeId Store ID (null for network managers)
     * @return Generated JWT token
     */
    public String generateToken(String username, String role, Integer storeId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        if (storeId != null) {
            claims.put("storeId", storeId);
        }
        return createToken(claims, username);
    }

    /**
     * @brief Create JWT token with claims
     * @param claims Custom claims
     * @param subject Token subject (username)
     * @return Created JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * @brief Validate JWT token
     * @param token JWT token
     * @param username Username to validate against
     * @return True if valid
     */
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * @brief Validate JWT token (without username check)
     * @param token JWT token
     * @return True if valid and not expired
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
