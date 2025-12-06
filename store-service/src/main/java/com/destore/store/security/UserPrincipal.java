package com.destore.store.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @file UserPrincipal.java
 * @brief Represents authenticated user information
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal {
    private String username;
    private String role;
    private Integer storeId;

    /** 
     * @return boolean
     */
    public boolean isNetworkManager() {
        return "NETWORK_MANAGER".equals(role);
    }

    /** 
     * @return boolean
     */
    public boolean isStoreManager() {
        return "STORE_MANAGER".equals(role);
    }
}
