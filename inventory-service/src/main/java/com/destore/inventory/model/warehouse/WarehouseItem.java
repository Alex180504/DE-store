package com.destore.inventory.model.warehouse;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Warehouse Item entity - Read-Only.
 * <p>
 * Maps to the {@code items} table in the warehouse database.
 * Represents an inventory item with its details and current stock quantity.
 * </p>
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
     * Checks if the item is out of stock.
     *
     * @return true if stock quantity is 0 or less, false otherwise
     */
    public boolean isOutOfStock() {
        return stockQuantity != null && stockQuantity <= 0;
    }

    /**
     * Checks if the item is in low stock status.
     * <p>
     * Returns true if stock is greater than the critical threshold but less than or equal to the low threshold.
     * </p>
     *
     * @param criticalThreshold the threshold for critical stock
     * @param lowThreshold      the threshold for low stock
     * @return true if item is low stock, false otherwise
     */
    public boolean isLowStock(int criticalThreshold, int lowThreshold) {
        return stockQuantity != null
                && stockQuantity > criticalThreshold
                && stockQuantity <= lowThreshold;
    }

    /**
     * Checks if the item is in critical stock status.
     * <p>
     * Returns true if stock is greater than 0 but less than or equal to the critical threshold.
     * </p>
     *
     * @param criticalThreshold the threshold for critical stock
     * @return true if item is critical stock, false otherwise
     */
    public boolean isCriticalStock(int criticalThreshold) {
        return stockQuantity != null
                && stockQuantity > 0
                && stockQuantity <= criticalThreshold;
    }
}
