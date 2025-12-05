package com.destore.auth.model.enums;

/**
 * Enum representing user roles in the DE-Store system.
 * <ul>
 * <li>NETWORK_MANAGER: Can manage global pricing rules across all stores</li>
 * <li>STORE_MANAGER: Can manage pricing rules for their assigned store only</li>
 * </ul>
 */
public enum UserRole {
    /**
     * Network Manager - manages global pricing rules
     */
    NETWORK_MANAGER,
    
    /**
     * Store Manager - manages store-specific pricing rules
     */
    STORE_MANAGER
}
