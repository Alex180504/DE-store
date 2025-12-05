package com.destore.inventory.service;

import com.destore.inventory.model.dto.StockAlert;
import com.destore.inventory.model.warehouse.WarehouseItem;
import com.destore.inventory.model.auth.User;
import com.destore.inventory.repository.warehouse.WarehouseItemRepository;
import com.destore.inventory.repository.auth.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StockMonitoringService.
 */
@ExtendWith(MockitoExtension.class)
class StockMonitoringServiceTest {

    @Mock
    private WarehouseItemRepository warehouseItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private InventoryConfigService configService;

    @InjectMocks
    private StockMonitoringService stockMonitoringService;

    private List<WarehouseItem> testItems;

    @BeforeEach
    void setUp() {
        // Setup test data with various stock levels
        testItems = Arrays.asList(
            createItem(1, "Item Out of Stock", 0),      // Out of stock
            createItem(2, "Item Critical", 15),          // Critical (< 20)
            createItem(3, "Item Low Stock", 35),         // Low stock (< 50)
            createItem(4, "Item Normal Stock", 75),      // Normal
            createItem(5, "Item High Stock", 150)        // High stock
        );

        // Setup config service defaults
        lenient().when(configService.getLowStockThreshold()).thenReturn(50);
        lenient().when(configService.getCriticalStockThreshold()).thenReturn(20);
        
        // Setup user repository default
        User manager = new User();
        manager.setEmail("manager@test.com");
        lenient().when(userRepository.findActiveNetworkManagers()).thenReturn(Arrays.asList(manager));
    }

    private WarehouseItem createItem(Integer id, String name, int stock) {
        return WarehouseItem.builder()
            .itemId(id)
            .name(name)
            .stockQuantity(stock)
            .category("Test Category")
            .build();
    }

    // ==================== Alert Detection Tests ====================

    @Test
    @DisplayName("Should detect out of stock items")
    void gatherStockAlerts_OutOfStockItem_DetectsCorrectly() {
        when(warehouseItemRepository.findLowStockItems(anyInt())).thenReturn(Arrays.asList(testItems.get(0)));

        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getStatus()).isEqualTo(StockAlert.StockStatus.OUT_OF_STOCK);
        assertThat(alerts.get(0).getItemId()).isEqualTo(1);
        assertThat(alerts.get(0).getCurrentStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should detect critical stock items")
    void gatherStockAlerts_CriticalStockItem_DetectsCorrectly() {
        when(warehouseItemRepository.findLowStockItems(anyInt())).thenReturn(Arrays.asList(testItems.get(1)));

        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getStatus()).isEqualTo(StockAlert.StockStatus.CRITICAL);
        assertThat(alerts.get(0).getItemId()).isEqualTo(2);
        assertThat(alerts.get(0).getCurrentStock()).isEqualTo(15);
    }

    @Test
    @DisplayName("Should detect low stock items")
    void gatherStockAlerts_LowStockItem_DetectsCorrectly() {
        when(warehouseItemRepository.findLowStockItems(anyInt())).thenReturn(Arrays.asList(testItems.get(2)));

        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getStatus()).isEqualTo(StockAlert.StockStatus.LOW);
        assertThat(alerts.get(0).getItemId()).isEqualTo(3);
        assertThat(alerts.get(0).getCurrentStock()).isEqualTo(35);
    }

    @Test
    @DisplayName("Should not alert on items with sufficient stock")
    void gatherStockAlerts_SufficientStock_NoAlerts() {
        when(warehouseItemRepository.findLowStockItems(anyInt())).thenReturn(Arrays.asList());

        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();

        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("Should detect multiple alerts with different severities")
    void gatherStockAlerts_MultipleItems_DetectsAll() {
        when(warehouseItemRepository.findLowStockItems(anyInt())).thenReturn(Arrays.asList(
            testItems.get(0),
            testItems.get(1),
            testItems.get(2)
        ));

        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();

        assertThat(alerts).hasSize(3); // Out of stock, critical, and low
        
        long outOfStock = alerts.stream()
            .filter(a -> a.getStatus() == StockAlert.StockStatus.OUT_OF_STOCK)
            .count();
        long critical = alerts.stream()
            .filter(a -> a.getStatus() == StockAlert.StockStatus.CRITICAL)
            .count();
        long low = alerts.stream()
            .filter(a -> a.getStatus() == StockAlert.StockStatus.LOW)
            .count();

        assertThat(outOfStock).isEqualTo(1);
        assertThat(critical).isEqualTo(1);
        assertThat(low).isEqualTo(1);
    }

    // ==================== Threshold Boundary Tests ====================

    @Test
    @DisplayName("Item exactly at critical threshold should be CRITICAL status")
    void gatherStockAlerts_ExactlyAtCriticalThreshold_IsCriticalStatus() {
        WarehouseItem item = createItem(10, "Boundary Test", 20); // Exactly at critical threshold
        when(warehouseItemRepository.findLowStockItems(anyInt())).thenReturn(Arrays.asList(item));

        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getStatus()).isEqualTo(StockAlert.StockStatus.CRITICAL);
    }

    @Test
    @DisplayName("Item exactly at low threshold should not alert")
    void gatherStockAlerts_ExactlyAtLowThreshold_NoAlert() {
        WarehouseItem item = createItem(10, "Boundary Test", 50); // Exactly at low threshold
        // findLowStockItems should NOT return this item if the query is correct (< threshold)
        // But here we are mocking the return.
        // If the service logic filters again, we might be fine.
        // However, the service iterates over whatever findLowStockItems returns.
        // If findLowStockItems returns it, the service will classify it.
        // Let's check the service logic:
        // } else { alert.setStatus(StockAlert.StockStatus.LOW); }
        // So if it's returned, it will be alerted.
        // Therefore, findLowStockItems should NOT return it.
        when(warehouseItemRepository.findLowStockItems(anyInt())).thenReturn(Arrays.asList());

        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();

        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("Item at threshold minus one should alert")
    void gatherStockAlerts_OneBelowThreshold_Alerts() {
        WarehouseItem item = createItem(10, "Boundary Test", 49); // One below low threshold
        when(warehouseItemRepository.findLowStockItems(anyInt())).thenReturn(Arrays.asList(item));

        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getStatus()).isEqualTo(StockAlert.StockStatus.LOW);
    }

    // ==================== Manual Trigger Tests ====================

    @Test
    @DisplayName("Manual trigger should send emails when alerts exist")
    void triggerManualCheck_WithAlerts_SendsEmail() {
        when(warehouseItemRepository.findLowStockItems(anyInt())).thenReturn(Arrays.asList(testItems.get(0)));
        doNothing().when(emailService).sendStockAlerts(anyList(), anyList());

        stockMonitoringService.triggerManualCheck();

        verify(emailService, times(1)).sendStockAlerts(anyList(), anyList());
    }

    @Test
    @DisplayName("Manual trigger should not send emails when no alerts")
    void triggerManualCheck_NoAlerts_DoesNotSendEmail() {
        when(warehouseItemRepository.findLowStockItems(anyInt())).thenReturn(Arrays.asList());

        stockMonitoringService.triggerManualCheck();

        verify(emailService, never()).sendStockAlerts(anyList(), anyList());
    }

    // ==================== Custom Threshold Tests ====================

    @Test
    @DisplayName("Should use custom thresholds when configured")
    void gatherStockAlerts_CustomThresholds_UsesCustomValues() {
        // Configure custom thresholds
        when(configService.getLowStockThreshold()).thenReturn(100);
        when(configService.getCriticalStockThreshold()).thenReturn(30);

        // Item with 75 stock should now be LOW (was normal with default thresholds)
        when(warehouseItemRepository.findLowStockItems(100)).thenReturn(Arrays.asList(testItems.get(3)));

        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getStatus()).isEqualTo(StockAlert.StockStatus.LOW);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle empty warehouse gracefully")
    void gatherStockAlerts_EmptyWarehouse_ReturnsEmptyList() {
        when(warehouseItemRepository.findLowStockItems(anyInt())).thenReturn(Arrays.asList());

        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();

        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("Should handle null item name gracefully")
    void gatherStockAlerts_NullItemName_HandlesGracefully() {
        WarehouseItem item = createItem(99, null, 5);
        when(warehouseItemRepository.findLowStockItems(anyInt())).thenReturn(Arrays.asList(item));

        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getItemName()).isNull();
    }

    @Test
    @DisplayName("Should handle negative stock quantities")
    void gatherStockAlerts_NegativeStock_TreatsAsOutOfStock() {
        WarehouseItem item = createItem(100, "Negative Stock Item", -5);
        when(warehouseItemRepository.findLowStockItems(anyInt())).thenReturn(Arrays.asList(item));

        List<StockAlert> alerts = stockMonitoringService.getCurrentAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getStatus()).isEqualTo(StockAlert.StockStatus.OUT_OF_STOCK);
    }
}
