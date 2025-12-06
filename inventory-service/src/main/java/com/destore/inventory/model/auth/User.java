package com.destore.inventory.model.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User entity from Auth DB - Read-Only
 * Used to fetch network manager emails
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "role")
    private String role;

    @Column(name = "is_active")
    private Boolean isActive;

    /** 
     * @return boolean
     */
    public boolean isNetworkManager() {
        return "NETWORK_MANAGER".equals(role);
    }
}
