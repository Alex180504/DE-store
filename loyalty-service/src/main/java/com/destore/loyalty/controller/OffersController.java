package com.destore.loyalty.controller;

import com.destore.loyalty.dto.RedemptionOfferDTO;
import com.destore.loyalty.entity.RedemptionOffer;
import com.destore.loyalty.repository.RedemptionOfferRepository;
import com.destore.loyalty.repository.RedemptionUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for redemption offers.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/loyalty/offers")
@RequiredArgsConstructor
public class OffersController {

    private final RedemptionOfferRepository offerRepository;
    private final RedemptionUsageRepository usageRepository;

    /**
     * Gets all active redemption offers.
     *
     * @return List of active offers
     */
    @GetMapping("/active")
    public ResponseEntity<List<RedemptionOfferDTO>> getActiveOffers() {
        log.info("Fetching active redemption offers");

        LocalDateTime now = LocalDateTime.now();
        List<RedemptionOffer> offers = offerRepository.findActiveAvailableOffers(now);

        List<RedemptionOfferDTO> dtos = offers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Gets applicable redemption offers for a specific item.
     *
     * @param itemId Item ID
     * @return List of applicable offers
     */
    @GetMapping("/applicable")
    public ResponseEntity<List<RedemptionOfferDTO>> getApplicableOffers(
            @RequestParam Integer itemId) {
        
        log.info("Fetching offers for item {}", itemId);

        LocalDateTime now = LocalDateTime.now();
        List<RedemptionOffer> offers = offerRepository.findApplicableOffers(itemId, now);

        List<RedemptionOfferDTO> dtos = offers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Gets customer-specific offer availability.
     *
     * @param customerId Customer ID
     * @return List of offers with customer usage info
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<RedemptionOfferDTO>> getCustomerOffers(@PathVariable Integer customerId) {
        log.info("Fetching offers for customer {}", customerId);

        LocalDateTime now = LocalDateTime.now();
        List<RedemptionOffer> offers = offerRepository.findActiveAvailableOffers(now);

        List<RedemptionOfferDTO> dtos = offers.stream()
                .map(offer -> {
                    RedemptionOfferDTO dto = mapToDTO(offer);
                    
                    // Check customer usage
                    usageRepository.findByCustomerIdAndRedemptionId(customerId, offer.getRedemptionId())
                            .ifPresent(usage -> {
                                boolean canUse = usage.canUse(offer.getMaxUsesPerCustomer());
                                dto.setAvailable(dto.getAvailable() && canUse);
                            });
                    
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Maps entity to DTO.
     *
     * @param offer Redemption offer entity
     * @return Redemption offer DTO
     */
    private RedemptionOfferDTO mapToDTO(RedemptionOffer offer) {
        return RedemptionOfferDTO.builder()
                .redemptionId(offer.getRedemptionId())
                .itemId(offer.getItemId())
                .offerName(offer.getOfferName())
                .description(offer.getDescription())
                .discountPercentage(offer.getDiscountPercentage())
                .pointsCost(offer.getPointsCost())
                .maxUsesPerCustomer(offer.getMaxUsesPerCustomer())
                .maxTotalUses(offer.getMaxTotalUses())
                .currentTotalUses(offer.getCurrentTotalUses())
                .validFrom(offer.getValidFrom())
                .validTo(offer.getValidTo())
                .available(offer.hasAvailableUses())
                .build();
    }
}
