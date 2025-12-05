package com.destore.shopping.service;

import com.destore.shopping.model.BasketPricingRequest;
import com.destore.shopping.model.BasketPricingResponse;
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

    /**
     * Trigger manual points calculation for a customer
     *
     * @param customerId the customer ID
     */
    public void calculateCustomerPoints(Integer customerId) {
        log.info("Triggering points calculation for customer {}", customerId);
        
        webClient.post()
                .uri("/api/loyalty/points/calculate/{customerId}", customerId)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(result -> log.info("Points calculation completed for customer {}", customerId))
                .doOnError(error -> log.error("Error calculating points: {}", error.getMessage()))
                .block();
    }

    /**
     * Price a basket with redemption offers
     *
     * @param request the basket pricing request
     * @return basket pricing response with discounts
     */
    public BasketPricingResponse priceBasket(BasketPricingRequest request) {
        log.info("Pricing basket for customer {} with {} items", 
                request.customerId, request.items != null ? request.items.size() : 0);
        
        return webClient.post()
                .uri("/api/loyalty/basket/price")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(BasketPricingResponse.class)
                .doOnSuccess(response -> log.info("Basket priced: {} points deducted, reference: {}", 
                        response.totalPointsDeducted, response.basketReference))
                .doOnError(error -> log.error("Error pricing basket: {}", error.getMessage()))
                .block();
    }
}
