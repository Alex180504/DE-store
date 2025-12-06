package com.destore.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @file LoginResponse.java
 * @brief DTO for successful login responses
 * 
 * Contains the JWT token and user information
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    /**
     * @brief JWT authentication token
     */
    private String token;

    /**
     * @brief Username of the authenticated user
     */
    private String username;

    /**
     * @brief Full name of the user
     */
    private String fullName;

    /**
     * @brief User role (NETWORK_MANAGER or STORE_MANAGER)
     */
    private String role;

    /**
     * @brief Store ID (only for STORE_MANAGER, null for NETWORK_MANAGER)
     */
    private Integer storeId;
}
