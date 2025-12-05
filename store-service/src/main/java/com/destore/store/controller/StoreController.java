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
 * REST controller for store management.
 */
@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    /**
     * Gets all stores.
     *
     * @param activeOnly If true, returns only active stores
     * @return List of stores
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
     * Gets store by ID.
     *
     * @param storeId Store ID
     * @return Store details
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStoreById(@PathVariable Integer storeId) {
        StoreResponse store = storeService.getStoreById(storeId);
        return ResponseEntity.ok(store);
    }

    /**
     * Gets store by code.
     *
     * @param storeCode Store code
     * @return Store details
     */
    @GetMapping("/code/{storeCode}")
    public ResponseEntity<StoreResponse> getStoreByCode(@PathVariable String storeCode) {
        StoreResponse store = storeService.getStoreByCode(storeCode);
        return ResponseEntity.ok(store);
    }

    /**
     * Creates new store (Network Manager only).
     *
     * @param request Store creation request
     * @return Created store details
     */
    @PostMapping
    public ResponseEntity<StoreResponse> createStore(@Valid @RequestBody StoreRequest request) {
        StoreResponse store = storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(store);
    }

    /**
     * Updates store (Network Manager only).
     *
     * @param storeId Store ID
     * @param request Store update request
     * @return Updated store details
     */
    @PutMapping("/{storeId}")
    public ResponseEntity<StoreResponse> updateStore(
            @PathVariable Integer storeId,
            @Valid @RequestBody StoreRequest request) {
        StoreResponse store = storeService.updateStore(storeId, request);
        return ResponseEntity.ok(store);
    }

    /**
     * Deletes store (Network Manager only).
     *
     * @param storeId Store ID
     * @return No content
     */
    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable Integer storeId) {
        storeService.deleteStore(storeId);
        return ResponseEntity.noContent().build();
    }
}
