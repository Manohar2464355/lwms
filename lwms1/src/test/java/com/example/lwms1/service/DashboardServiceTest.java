package com.example.lwms1.service;

import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.Shipment;
import com.example.lwms1.model.Space;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock private InventoryService inventoryService;
    @Mock private ShipmentService shipmentService;
    @Mock private MaintenanceService maintenanceService;
    @Mock private ReportService reportService;
    @Mock private SpaceService spaceService;
    @Mock private UserService userService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Stats: Should calculate 50% utilization when half capacity is used")
    void testUtilizationCalculation() {
        // Arrange
        Space s1 = new Space();
        s1.setUsedCapacity(50);
        s1.setTotalCapacity(100);

        Space s2 = new Space();
        s2.setUsedCapacity(25);
        s2.setTotalCapacity(50);

        // Half of total (75/150) = 50%
        when(spaceService.listAll()).thenReturn(Arrays.asList(s1, s2));

        // Mocking other services to return empty lists/0 to avoid NullPointer
        when(inventoryService.listAll()).thenReturn(Collections.emptyList());
        when(shipmentService.listAll()).thenReturn(Collections.emptyList());
        when(maintenanceService.getPendingMaintenanceCount()).thenReturn(0L);
        when(reportService.listAll()).thenReturn(Collections.emptyList());
        when(userService.listAll()).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> stats = dashboardService.getAllStats();

        // Assert
        assertEquals("50.0%", stats.get("warehouseUtilization"));
    }

    @Test
    @DisplayName("Stats: Should return 0.0% utilization if no spaces exist")
    void testZeroUtilization() {
        when(spaceService.listAll()).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> stats = dashboardService.getAllStats();

        // Assert
        assertEquals("0.0%", stats.get("warehouseUtilization"));
    }

    @Test
    @DisplayName("Stats: Should correctly count items from all services")
    void testBasicCounts() {
        // 1. Use the actual Entity or DTO classes defined in your project
        // Replace 'Inventory' and 'Shipment' with your actual class names
        Inventory item1 = new Inventory();
        Inventory item2 = new Inventory();
        Shipment shipment1 = new Shipment();

        // 2. Mocking with the correct types
        when(inventoryService.listAll()).thenReturn(Arrays.asList(item1, item2));
        when(shipmentService.listAll()).thenReturn(Collections.singletonList(shipment1));

        // The rest of your mocks
        when(maintenanceService.getPendingMaintenanceCount()).thenReturn(5L);
        when(userService.listAll()).thenReturn(Collections.emptyList());
        when(reportService.listAll()).thenReturn(Collections.emptyList());
        when(spaceService.listAll()).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> stats = dashboardService.getAllStats();

        // Assert
        assertEquals(2, stats.get("inventoryCount"));
        assertEquals(1, stats.get("activeShipmentsCount"));
        assertEquals(5L, stats.get("openMaintenanceTasks"));
    }
}