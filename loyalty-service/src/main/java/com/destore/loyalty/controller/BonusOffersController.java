package com.destore.loyalty.controller;

import com.destore.loyalty.dto.BonusOfferRequest;
import com.destore.loyalty.dto.BonusOfferResponse;
import com.destore.loyalty.entity.BonusOffer;
import com.destore.loyalty.repository.BonusOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for bonus offer management.
 *
 * @author DE-Store Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/loyalty/bonuses")
@RequiredArgsConstructor
public class BonusOffersController {

    private final BonusOfferRepository bonusOfferRepository;

    /**
     * Get all bonus offers.
     *
     * @return List of all bonus offers
     */
    @GetMapping
    public ResponseEntity<List<BonusOfferResponse>> getAllBonuses() {
        log.info("Fetching all bonus offers");
        
        List<BonusOffer> offers = bonusOfferRepository.findAll();
        List<BonusOfferResponse> responses = offers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get active bonus offers.
     *
     * @return List of active bonus offers
     */
    @GetMapping("/active")
    public ResponseEntity<List<BonusOfferResponse>> getActiveBonuses() {
        log.info("Fetching active bonus offers");
        
        LocalDateTime now = LocalDateTime.now();
        List<BonusOffer> offers = bonusOfferRepository.findActiveOffers(now);
        List<BonusOfferResponse> responses = offers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get bonus offer by ID.
     *
     * @param id Offer ID
     * @return Bonus offer
     */
    @GetMapping("/{id}")
    public ResponseEntity<BonusOfferResponse> getBonusById(@PathVariable Integer id) {
        log.info("Fetching bonus offer with ID: {}", id);
        
        return bonusOfferRepository.findById(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new bonus offer.
     *
     * @param request Bonus offer request
     * @return Created bonus offer
     */
    @PostMapping
    public ResponseEntity<BonusOfferResponse> createBonus(@Valid @RequestBody BonusOfferRequest request) {
        log.info("Creating new bonus offer: {}", request.getOfferName());
        
        BonusOffer offer = BonusOffer.builder()
                .storeId(request.getStoreId())
                .offerName(request.getOfferName())
                .thresholdAmount(request.getThresholdAmount())
                .bonusPoints(request.getBonusPoints())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .isActive(request.getIsActive())
                .build();
        
        BonusOffer saved = bonusOfferRepository.save(offer);
        log.info("Created bonus offer with ID: {}", saved.getOfferId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved));
    }

    /**
     * Update an existing bonus offer.
     *
     * @param id Offer ID
     * @param request Updated bonus offer data
     * @return Updated bonus offer
     */
    @PutMapping("/{id}")
    public ResponseEntity<BonusOfferResponse> updateBonus(
            @PathVariable Integer id,
            @Valid @RequestBody BonusOfferRequest request) {
        
        log.info("Updating bonus offer with ID: {}", id);
        
        return bonusOfferRepository.findById(id)
                .map(existing -> {
                    existing.setStoreId(request.getStoreId());
                    existing.setOfferName(request.getOfferName());
                    existing.setThresholdAmount(request.getThresholdAmount());
                    existing.setBonusPoints(request.getBonusPoints());
                    existing.setValidFrom(request.getValidFrom());
                    existing.setValidTo(request.getValidTo());
                    existing.setIsActive(request.getIsActive());
                    existing.setUpdatedAt(LocalDateTime.now());
                    
                    BonusOffer updated = bonusOfferRepository.save(existing);
                    log.info("Updated bonus offer with ID: {}", id);
                    
                    return ResponseEntity.ok(mapToResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a bonus offer.
     *
     * @param id Offer ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBonus(@PathVariable Integer id) {
        log.info("Deleting bonus offer with ID: {}", id);
        
        if (!bonusOfferRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        bonusOfferRepository.deleteById(id);
        log.info("Deleted bonus offer with ID: {}", id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle bonus offer active status.
     *
     * @param id Offer ID
     * @return Updated bonus offer
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<BonusOfferResponse> toggleBonusStatus(@PathVariable Integer id) {
        log.info("Toggling status for bonus offer with ID: {}", id);
        
        return bonusOfferRepository.findById(id)
                .map(offer -> {
                    offer.setIsActive(!offer.getIsActive());
                    offer.setUpdatedAt(LocalDateTime.now());
                    
                    BonusOffer updated = bonusOfferRepository.save(offer);
                    log.info("Toggled bonus offer {} to {}", id, updated.getIsActive() ? "active" : "inactive");
                    
                    return ResponseEntity.ok(mapToResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Map entity to response DTO.
     */
    private BonusOfferResponse mapToResponse(BonusOffer offer) {
        return BonusOfferResponse.builder()
                .offerId(offer.getOfferId())
                .storeId(offer.getStoreId())
                .offerName(offer.getOfferName())
                .thresholdAmount(offer.getThresholdAmount())
                .bonusPoints(offer.getBonusPoints())
                .validFrom(offer.getValidFrom())
                .validTo(offer.getValidTo())
                .isActive(offer.getIsActive())
                .createdAt(offer.getCreatedAt())
                .updatedAt(offer.getUpdatedAt())
                .build();
    }
}
