package com.destore.inventory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for threshold configuration response.
 * <p>
 * Carries the current configuration values for low and critical stock thresholds.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdConfig {
    private Integer lowStock;
    private Integer criticalStock;
}
