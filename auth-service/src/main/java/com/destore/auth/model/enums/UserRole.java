package com.destore.auth.model.enums;

/**
 * @file UserRole.java
 * @brief Enum representing user roles in the DE-Store system
 * 
 * - NETWORK_MANAGER: Can manage global pricing rules across all stores
 * - STORE_MANAGER: Can manage pricing rules for their assigned store only
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
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
