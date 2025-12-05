package com.destore.inventory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for stock alert information
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
