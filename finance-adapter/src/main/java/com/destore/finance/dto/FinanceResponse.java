package com.destore.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceResponse {
    
    private Boolean approved;
    private String reference;
    private Integer customerId;
    private BigDecimal amount;
    private String reason;
    private String timestamp;
}
