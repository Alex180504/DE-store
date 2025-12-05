package com.destore.inventory.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration for Inventory Service.
 * <p>
 * Configures the security filter chain, including:
 * <ul>
 *   <li>Endpoint Access:
 *     <ul>
 *       <li>Public: /api/inventory/health (health check)</li>
 *       <li>Public: /actuator/** (monitoring endpoints)</li>
 *       <li>NETWORK_MANAGER only: /api/inventory/admin/** (all inventory management)</li>
 *     </ul>
 *   </li>
 *   <li>Security Mechanisms:
 *     <ul>
 *       <li>JWT-based authentication</li>
 *       <li>Role-based authorization with @PreAuthorize annotations</li>
 *       <li>Stateless session (no cookies)</li>
 *       <li>All other endpoints denied by default</li>
 *     </ul>
 *   </li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructs a new SecurityConfig with the specified JWT authentication filter.
     *
     * @param jwtAuthenticationFilter the filter to validate JWT tokens
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configures the security filter chain.
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/api/inventory/health").permitAll()
                
                // Admin endpoints - require NETWORK_MANAGER role
                // Role-based access enforced via @PreAuthorize annotations
                .requestMatchers("/api/inventory/admin/**").authenticated()
                
                // Deny all other API requests
                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
