package com.destore.inventory.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Data Transfer Object for updating inventory thresholds.
 * <p>
 * Used to receive new threshold values from the API.
 * Note: Out-of-stock threshold is always 0 (hardcoded) and cannot be updated.
 * </p>
 */
@Data
public class ThresholdUpdateRequest {
    
    @NotNull(message = "Low stock threshold is required")
    @Min(value = 1, message = "Low stock threshold must be at least 1")
    private Integer lowStock;
    
    @NotNull(message = "Critical stock threshold is required")
    @Min(value = 0, message = "Critical stock threshold must be at least 0")
    private Integer criticalStock;
}
