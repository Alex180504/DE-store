package com.destore.loyalty.controller;

import com.destore.loyalty.dto.CustomerPointsDTO;
import com.destore.loyalty.entity.CustomerPointsBalance;
import com.destore.loyalty.repository.CustomerPointsBalanceRepository;
import com.destore.loyalty.service.PointsCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for customer loyalty points operations.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/loyalty/points")
@RequiredArgsConstructor
public class PointsController {

    private final CustomerPointsBalanceRepository balanceRepository;
    private final PointsCalculationService calculationService;

    /**
     * Gets customer points balance.
     *
     * @param customerId Customer ID
     * @return Customer points DTO
     */
    @GetMapping("/balance/{customerId}")
    public ResponseEntity<CustomerPointsDTO> getCustomerPoints(@PathVariable Long customerId) {
        log.info("Fetching points balance for customer {}", customerId);

        return balanceRepository.findByCustomerId(customerId)
                .map(balance -> ResponseEntity.ok(mapToDTO(balance)))
                .orElse(ResponseEntity.ok(CustomerPointsDTO.builder()
                        .customerId(customerId)
                        .currentBalance(java.math.BigDecimal.ZERO)
                        .lifetimeEarned(java.math.BigDecimal.ZERO)
                        .lifetimeRedeemed(java.math.BigDecimal.ZERO)
                        .build()));
    }

    /**
     * Triggers manual points calculation for a customer.
     *
     * @param customerId Customer ID
     * @return Updated customer points
     */
    @PostMapping("/calculate/{customerId}")
    public ResponseEntity<CustomerPointsDTO> calculatePoints(@PathVariable Long customerId) {
        log.info("Manual points calculation requested for customer {}", customerId);

        CustomerPointsBalance balance = calculationService.recalculateCustomerPoints(customerId);
        return ResponseEntity.ok(mapToDTO(balance));
    }

    /**
     * Maps entity to DTO.
     *
     * @param balance Customer points balance entity
     * @return Customer points DTO
     */
    private CustomerPointsDTO mapToDTO(CustomerPointsBalance balance) {
        return CustomerPointsDTO.builder()
                .customerId(balance.getCustomerId())
                .currentBalance(balance.getCurrentBalance())
                .lifetimeEarned(balance.getLifetimeEarned())
                .lifetimeRedeemed(balance.getLifetimeRedeemed())
                .build();
    }
}
