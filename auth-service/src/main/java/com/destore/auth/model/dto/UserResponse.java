package com.destore.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @file UserResponse.java
 * @brief DTO for user information responses
 * 
 * Used for returning user details (excludes password hash for security)
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    /**
     * @brief User ID
     */
    private Integer userId;

    /**
     * @brief Username
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

    /**
     * @brief Whether the user account is active
     */
    private Boolean isActive;

    /**
     * @brief Timestamp when the user was created
     */
    private LocalDateTime createdAt;
}
