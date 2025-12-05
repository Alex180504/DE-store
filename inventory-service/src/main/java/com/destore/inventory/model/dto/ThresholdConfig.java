package com.destore.inventory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for threshold configuration response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdConfig {
    private Integer lowStock;
    private Integer criticalStock;
}
