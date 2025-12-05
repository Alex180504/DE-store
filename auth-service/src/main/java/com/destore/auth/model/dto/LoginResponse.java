package com.destore.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for successful login responses.
 * <p>
 * Contains the JWT token and user information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    /**
     * JWT authentication token.
     */
    private String token;

    /**
     * Username of the authenticated user.
     */
    private String username;

    /**
     * Full name of the user.
     */
    private String fullName;

    /**
     * User role (NETWORK_MANAGER or STORE_MANAGER).
     */
    private String role;

    /**
     * Store ID (only for STORE_MANAGER, null for NETWORK_MANAGER).
     */
    private Integer storeId;
}
