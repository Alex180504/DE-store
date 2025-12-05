package com.destore.finance.service;

import com.destore.finance.dto.FinanceRequest;
import com.destore.finance.dto.FinanceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class EnablingClient {
    
    private final WebClient webClient;
    
    public EnablingClient(@Value("${enabling.api.url}") String enablingApiUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(enablingApiUrl)
                .build();
    }
    
    public FinanceResponse requestApproval(FinanceRequest request) {
        log.info("Sending finance request to Enabling for customer {}: £{}", 
                request.getCustomerId(), request.getAmount());
        
        try {
            FinanceResponse response = webClient.post()
                    .uri("/api/finance/approve")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(FinanceResponse.class)
                    .block();
            
            log.info("Enabling response: {} - {}", 
                    response.getApproved() ? "APPROVED" : "DENIED", 
                    response.getReference());
            
            return response;
        } catch (Exception e) {
            log.error("Error calling Enabling API", e);
            return FinanceResponse.builder()
                    .approved(false)
                    .customerId(request.getCustomerId())
                    .amount(request.getAmount())
                    .reason("Service unavailable")
                    .build();
        }
    }
}
