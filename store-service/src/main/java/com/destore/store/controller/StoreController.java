package com.destore.store.controller;

import com.destore.store.model.dto.StoreRequest;
import com.destore.store.model.dto.StoreResponse;
import com.destore.store.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @file StoreController.java
 * @brief REST controller for store management
 * 
 * @author DE-Store Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    /**
     * Get all stores
     */
    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAllStores(
            @RequestParam(required = false) Boolean activeOnly) {
        List<StoreResponse> stores = activeOnly != null && activeOnly
                ? storeService.getActiveStores()
                : storeService.getAllStores();
        return ResponseEntity.ok(stores);
    }

    /**
     * Get store by ID
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStoreById(@PathVariable Integer storeId) {
        StoreResponse store = storeService.getStoreById(storeId);
        return ResponseEntity.ok(store);
    }

    /**
     * Get store by code
     */
    @GetMapping("/code/{storeCode}")
    public ResponseEntity<StoreResponse> getStoreByCode(@PathVariable String storeCode) {
        StoreResponse store = storeService.getStoreByCode(storeCode);
        return ResponseEntity.ok(store);
    }

    /**
     * Create new store (Network Manager only)
     */
    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@Valid @RequestBody StoreRequest request) {
        StoreResponse store = storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(store);
    }

    /**
     * Update store (Network Manager only)
     */
    @PutMapping("/{storeId}")
    public ResponseEntity<StoreResponse> updateStore(
            @PathVariable Integer storeId,
            @Valid @RequestBody StoreRequest request) {
        StoreResponse store = storeService.updateStore(storeId, request);
        return ResponseEntity.ok(store);
    }

    /**
     * Delete store (Network Manager only)
     */
    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable Integer storeId) {
        storeService.deleteStore(storeId);
        return ResponseEntity.noContent().build();
    }
}
