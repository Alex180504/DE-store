package com.destore.loyalty.controller;

import com.destore.loyalty.dto.RedemptionOfferDTO;
import com.destore.loyalty.dto.RedemptionOfferRequest;
import com.destore.loyalty.entity.RedemptionOffer;
import com.destore.loyalty.repository.RedemptionOfferRepository;
import com.destore.loyalty.repository.RedemptionUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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
                    usageRepository.findByCustomerIdAndRedemptionOfferId(customerId, offer.getRedemptionId())
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
     * Get all redemption offers.
     *
     * @return List of all redemption offers
     */
    @GetMapping
    public ResponseEntity<List<RedemptionOfferDTO>> getAllOffers() {
        log.info("Fetching all redemption offers");
        
        List<RedemptionOffer> offers = offerRepository.findAll();
        List<RedemptionOfferDTO> dtos = offers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get redemption offer by ID.
     *
     * @param id Offer ID
     * @return Redemption offer
     */
    @GetMapping("/{id}")
    public ResponseEntity<RedemptionOfferDTO> getOfferById(@PathVariable Integer id) {
        log.info("Fetching redemption offer with ID: {}", id);
        
        return offerRepository.findById(id)
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new redemption offer.
     *
     * @param request Redemption offer request
     * @return Created redemption offer
     */
    @PostMapping
    public ResponseEntity<RedemptionOfferDTO> createOffer(@Valid @RequestBody RedemptionOfferRequest request) {
        log.info("Creating new redemption offer: {}", request.getOfferName());
        
        RedemptionOffer offer = RedemptionOffer.builder()
                .itemId(request.getItemId())
                .offerName(request.getOfferName())
                .description(request.getDescription())
                .discountPercentage(request.getDiscountPercentage())
                .pointsCost(request.getPointsCost())
                .maxUsesPerCustomer(request.getMaxUsesPerCustomer())
                .maxTotalUses(request.getMaxTotalUses())
                .currentTotalUses(0)
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .isActive(request.getIsActive())
                .build();
        
        RedemptionOffer saved = offerRepository.save(offer);
        log.info("Created redemption offer with ID: {}", saved.getRedemptionId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(saved));
    }

    /**
     * Update an existing redemption offer.
     *
     * @param id Offer ID
     * @param request Updated redemption offer data
     * @return Updated redemption offer
     */
    @PutMapping("/{id}")
    public ResponseEntity<RedemptionOfferDTO> updateOffer(
            @PathVariable Integer id,
            @Valid @RequestBody RedemptionOfferRequest request) {
        
        log.info("Updating redemption offer with ID: {}", id);
        
        return offerRepository.findById(id)
                .map(existing -> {
                    existing.setItemId(request.getItemId());
                    existing.setOfferName(request.getOfferName());
                    existing.setDescription(request.getDescription());
                    existing.setDiscountPercentage(request.getDiscountPercentage());
                    existing.setPointsCost(request.getPointsCost());
                    existing.setMaxUsesPerCustomer(request.getMaxUsesPerCustomer());
                    existing.setMaxTotalUses(request.getMaxTotalUses());
                    existing.setValidFrom(request.getValidFrom());
                    existing.setValidTo(request.getValidTo());
                    existing.setIsActive(request.getIsActive());
                    existing.setUpdatedAt(LocalDateTime.now());
                    
                    RedemptionOffer updated = offerRepository.save(existing);
                    log.info("Updated redemption offer with ID: {}", id);
                    
                    return ResponseEntity.ok(mapToDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a redemption offer.
     *
     * @param id Offer ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Integer id) {
        log.info("Deleting redemption offer with ID: {}", id);
        
        if (!offerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        offerRepository.deleteById(id);
        log.info("Deleted redemption offer with ID: {}", id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle redemption offer active status.
     *
     * @param id Offer ID
     * @return Updated redemption offer
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RedemptionOfferDTO> toggleOfferStatus(@PathVariable Integer id) {
        log.info("Toggling status for redemption offer with ID: {}", id);
        
        return offerRepository.findById(id)
                .map(offer -> {
                    offer.setIsActive(!offer.getIsActive());
                    offer.setUpdatedAt(LocalDateTime.now());
                    
                    RedemptionOffer updated = offerRepository.save(offer);
                    log.info("Toggled redemption offer {} to {}", id, updated.getIsActive() ? "active" : "inactive");
                    
                    return ResponseEntity.ok(mapToDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
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
