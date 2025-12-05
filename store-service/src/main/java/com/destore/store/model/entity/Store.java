package com.destore.store.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a store location.
 */
@Entity
@Table(name = "stores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    /**
     * Store ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Integer storeId;

    /**
     * Store code (e.g., LON-001).
     */
    @Column(name = "store_code", nullable = false, unique = true, length = 20)
    private String storeCode;

    /**
     * Store name.
     */
    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    /**
     * Store address.
     */
    @Column(name = "address", nullable = false)
    private String address;

    /**
     * Store postcode.
     */
    @Column(name = "postcode", nullable = false, length = 10)
    private String postcode;

    /**
     * Whether the store is active.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Creation timestamp.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Sets timestamps before persisting.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Sets timestamp before updating.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
