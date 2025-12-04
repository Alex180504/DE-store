package com.destore.inventory.model.warehouse;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Warehouse Item entity - Read-Only
 * Maps to items table in warehouse database
 */
@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseItem {

    @Id
    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Stock status helpers
     */
    public boolean isOutOfStock() {
        return stockQuantity != null && stockQuantity == 0;
    }

    /**
     * Returns true if stock is between criticalThreshold + 1 and lowThreshold (inclusive).
     */
    public boolean isLowStock(int criticalThreshold, int lowThreshold) {
        return stockQuantity != null
                && stockQuantity > criticalThreshold
                && stockQuantity <= lowThreshold;
    }

    /**
     * Returns true if stock is between 1 and criticalThreshold (inclusive).
     */
    public boolean isCriticalStock(int criticalThreshold) {
        return stockQuantity != null
                && stockQuantity > 0
                && stockQuantity <= criticalThreshold;
    }
}
