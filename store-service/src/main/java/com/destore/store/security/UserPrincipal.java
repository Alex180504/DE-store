package com.destore.store.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents authenticated user information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal {
    private String username;
    private String role;
    private Integer storeId;

    public boolean isNetworkManager() {
        return "NETWORK_MANAGER".equals(role);
    }

    public boolean isStoreManager() {
        return "STORE_MANAGER".equals(role);
    }
}
