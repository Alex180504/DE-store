package com.destore.pricing.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @file UserPrincipal.java
 * @brief Custom user principal containing JWT claims
 * 
 * Holds authenticated user information extracted from JWT:
 * - username: User identifier
 * - role: NETWORK_MANAGER or STORE_MANAGER
 * - storeId: Store ID (null for network managers)
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public class UserPrincipal {
    private final String username;
    private final String role;
    private final Integer storeId;

    /**
     * @brief Check if user is a network manager
     * @return true if NETWORK_MANAGER, false otherwise
     */
    public boolean isNetworkManager() {
        return "NETWORK_MANAGER".equals(role);
    }

    /**
     * @brief Check if user is a store manager
     * @return true if STORE_MANAGER, false otherwise
     */
    public boolean isStoreManager() {
        return "STORE_MANAGER".equals(role);
    }
}
