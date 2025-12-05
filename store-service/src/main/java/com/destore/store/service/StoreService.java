package com.destore.store.service;

import com.destore.store.exception.ForbiddenAccessException;
import com.destore.store.exception.ResourceNotFoundException;
import com.destore.store.model.dto.StoreRequest;
import com.destore.store.model.dto.StoreResponse;
import com.destore.store.model.entity.Store;
import com.destore.store.repository.StoreRepository;
import com.destore.store.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for store management.
 */
@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        throw new ForbiddenAccessException("User not authenticated");
    }

    /**
     * Gets all stores.
     *
     * @return List of all stores
     */
    public List<StoreResponse> getAllStores() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets active stores only.
     *
     * @return List of active stores
     */
    public List<StoreResponse> getActiveStores() {
        List<Store> stores = storeRepository.findByIsActive(true);
        return stores.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets store by ID.
     *
     * @param storeId Store ID
     * @return Store details
     * @throws ResourceNotFoundException if store not found
     */
    public StoreResponse getStoreById(Integer storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));
        return mapToResponse(store);
    }

    /**
     * Gets store by code.
     *
     * @param storeCode Store code
     * @return Store details
     * @throws ResourceNotFoundException if store not found
     */
    public StoreResponse getStoreByCode(String storeCode) {
        Store store = storeRepository.findByStoreCode(storeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with code: " + storeCode));
        return mapToResponse(store);
    }

    /**
     * Creates new store (Network Manager only).
     *
     * @param request Store creation request
     * @return Created store details
     * @throws ForbiddenAccessException if user is not a network manager
     * @throws IllegalArgumentException if store code already exists
     */
    @Transactional
    public StoreResponse createStore(StoreRequest request) {
        UserPrincipal user = getCurrentUser();

        // Only network managers can create stores
        if (!user.isNetworkManager()) {
            throw new ForbiddenAccessException("Only network managers can create stores");
        }

        // Check if store code already exists
        if (storeRepository.existsByStoreCode(request.getStoreCode())) {
            throw new IllegalArgumentException("Store code already exists: " + request.getStoreCode());
        }

        Store store = Store.builder()
                .storeCode(request.getStoreCode())
                .storeName(request.getStoreName())
                .address(request.getAddress())
                .postcode(request.getPostcode())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Store savedStore = storeRepository.save(store);
        return mapToResponse(savedStore);
    }

    /**
     * Updates store (Network Manager only).
     *
     * @param storeId Store ID
     * @param request Store update request
     * @return Updated store details
     * @throws ForbiddenAccessException  if user is not a network manager
     * @throws ResourceNotFoundException if store not found
     * @throws IllegalArgumentException  if new store code already exists
     */
    @Transactional
    public StoreResponse updateStore(Integer storeId, StoreRequest request) {
        UserPrincipal user = getCurrentUser();

        // Only network managers can update stores
        if (!user.isNetworkManager()) {
            throw new ForbiddenAccessException("Only network managers can update stores");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));

        // Check if new store code already exists (if changing)
        if (!store.getStoreCode().equals(request.getStoreCode()) &&
            storeRepository.existsByStoreCode(request.getStoreCode())) {
            throw new IllegalArgumentException("Store code already exists: " + request.getStoreCode());
        }

        store.setStoreCode(request.getStoreCode());
        store.setStoreName(request.getStoreName());
        store.setAddress(request.getAddress());
        store.setPostcode(request.getPostcode());
        if (request.getIsActive() != null) {
            store.setIsActive(request.getIsActive());
        }

        Store updatedStore = storeRepository.save(store);
        return mapToResponse(updatedStore);
    }

    /**
     * Deletes store (Network Manager only).
     *
     * @param storeId Store ID
     * @throws ForbiddenAccessException  if user is not a network manager
     * @throws ResourceNotFoundException if store not found
     */
    @Transactional
    public void deleteStore(Integer storeId) {
        UserPrincipal user = getCurrentUser();

        // Only network managers can delete stores
        if (!user.isNetworkManager()) {
            throw new ForbiddenAccessException("Only network managers can delete stores");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));

        storeRepository.delete(store);
    }

    private StoreResponse mapToResponse(Store store) {
        return StoreResponse.builder()
                .storeId(store.getStoreId())
                .storeCode(store.getStoreCode())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .postcode(store.getPostcode())
                .isActive(store.getIsActive())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
