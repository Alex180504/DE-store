package com.destore.shopping.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Basket Item DTO
 * <p>
 * Represents a single item in the shopping basket.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketItemDTO {
    
    private Integer itemId;
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private BigDecimal discountedPrice;
    private Integer appliedOfferId;
}
