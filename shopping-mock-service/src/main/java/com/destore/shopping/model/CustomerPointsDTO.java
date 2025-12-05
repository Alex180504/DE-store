package com.destore.shopping.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer Points Balance DTO
 * <p>
 * Represents a customer's loyalty points balance.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPointsDTO {
    
    private Integer customerId;
    private Integer currentBalance;
    private Integer lifetimeEarned;
    private Integer lifetimeRedeemed;
}
