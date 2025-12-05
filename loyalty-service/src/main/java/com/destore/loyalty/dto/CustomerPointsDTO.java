package com.destore.loyalty.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * DTO for customer loyalty points balance information.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPointsDTO {

    /**
     * Customer ID.
     */
    private Long customerId;

    /**
     * Current available points balance.
     */
    private BigDecimal currentBalance;

    /**
     * Total points earned over customer lifetime.
     */
    private BigDecimal lifetimeEarned;

    /**
     * Total points redeemed over customer lifetime.
     */
    private BigDecimal lifetimeRedeemed;
}
