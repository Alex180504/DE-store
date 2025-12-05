package com.destore.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user information responses.
 * <p>
 * Used for returning user details (excludes password hash for security).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    /**
     * User ID.
     */
    private Integer userId;

    /**
     * Username.
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

    /**
     * Whether the user account is active.
     */
    private Boolean isActive;

    /**
     * Timestamp when the user was created.
     */
    private LocalDateTime createdAt;
}
