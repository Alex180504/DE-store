package com.destore.finance.controller;

import com.destore.finance.dto.FinanceRequest;
import com.destore.finance.dto.FinanceResponse;
import com.destore.finance.service.EnablingClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Finance", description = "Finance approval operations")
public class FinanceController {
    
    private final EnablingClient enablingClient;
    
    @PostMapping("/request")
    @Operation(summary = "Request finance approval", 
               description = "Submit finance request to Enabling BNPL system")
    public ResponseEntity<FinanceResponse> requestFinance(@Valid @RequestBody FinanceRequest request) {
        log.info("Finance request received for customer {}", request.getCustomerId());
        
        FinanceResponse response = enablingClient.requestApproval(request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Finance Adapter is UP");
    }
}
