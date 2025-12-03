package com.destore.pricing.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * @file Store.java
 * @brief JPA Entity representing a retail store location
 * 
 * Stores can have specific pricing rules that override global network-wide pricing.
 * Each store is uniquely identified by store_code (e.g., 'LON-001').
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    /**
     * @brief Unique store identifier (auto-generated)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Integer storeId;

    /**
     * @brief Unique store code (e.g., 'LON-001', 'MAN-001')
     */
    @Column(name = "store_code", nullable = false, unique = true, length = 20)
    private String storeCode;

    /**
     * @brief Human-readable store name
     */
    @Column(name = "store_name", nullable = false)
    private String storeName;

    /**
     * @brief Store location/address
     */
    @Column(name = "location")
    private String location;

    /**
     * @brief Whether the store is currently active
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * @brief Timestamp when the store record was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * @brief Timestamp when the store record was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
