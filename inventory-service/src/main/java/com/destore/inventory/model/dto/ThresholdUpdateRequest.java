package com.destore.inventory.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for updating inventory thresholds
 */
@Data
public class ThresholdUpdateRequest {
    
    @NotNull(message = "Low stock threshold is required")
    @Min(value = 1, message = "Low stock threshold must be at least 1")
    private Integer lowStock;
    
    @NotNull(message = "Critical stock threshold is required")
    @Min(value = 0, message = "Critical stock threshold must be at least 0")
    private Integer criticalStock;
    
    @NotNull(message = "Out of stock threshold is required")
    @Min(value = 0, message = "Out of stock threshold must be 0")
    private Integer outOfStock;
}
