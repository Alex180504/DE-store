package com.destore.inventory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for stock alert information.
 * <p>
 * Contains details about an item that has triggered a stock alert,
 * including its current stock level and status (LOW, CRITICAL, OUT_OF_STOCK).
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAlert {
    
    private Integer itemId;
    private String itemName;
    private String category;
    private Integer currentStock;
    private StockStatus status;
    
    public enum StockStatus {
        OUT_OF_STOCK,
        CRITICAL,
        LOW
    }
}
