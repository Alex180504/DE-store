package com.destore.shopping.service;

import com.destore.shopping.model.CustomerPointsDTO;
import com.destore.shopping.model.RedemptionOfferDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Loyalty Service Client
 * <p>
 * Handles communication with the loyalty service API.
 * Provides methods for fetching offers and customer points balance.
 * </p>
 *
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@Service
public class LoyaltyServiceClient {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyServiceClient.class);

    private final WebClient webClient;

    public LoyaltyServiceClient(WebClient.Builder webClientBuilder,
                                @Value("${loyalty.service.url}") String loyaltyServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(loyaltyServiceUrl).build();
    }

    /**
     * Get all active redemption offers from loyalty service
     *
     * @return list of active redemption offers
     */
    public List<RedemptionOfferDTO> getActiveOffers() {
        log.info("Fetching active loyalty offers");
        
        return webClient.get()
                .uri("/api/loyalty/offers/active")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<RedemptionOfferDTO>>() {})
                .doOnSuccess(offers -> log.info("Retrieved {} active offers", offers.size()))
                .doOnError(error -> log.error("Error fetching offers: {}", error.getMessage()))
                .onErrorReturn(List.of())
                .block();
    }

    /**
     * Get customer points balance
     *
     * @param customerId the customer ID
     * @return customer points balance
     */
    public CustomerPointsDTO getCustomerPoints(Integer customerId) {
        log.info("Fetching points balance for customer {}", customerId);
        
        return webClient.get()
                .uri("/api/loyalty/points/balance/{customerId}", customerId)
                .retrieve()
                .bodyToMono(CustomerPointsDTO.class)
                .doOnSuccess(points -> log.info("Customer {} has {} points", customerId, points.getCurrentBalance()))
                .doOnError(error -> log.error("Error fetching customer points: {}", error.getMessage()))
                .onErrorReturn(new CustomerPointsDTO(customerId, 0, 0, 0))
                .block();
    }
}
