package com.destore.shopping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Basket DTO for shopping requests
 * <p>
 * Represents a customer's shopping basket with items and selected loyalty offers.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketDTO {
    
    private Integer customerId;
    private Integer storeId;
    private List<BasketItemDTO> items;
    private List<Integer> selectedOfferIds;
    private String basketReference;
}
