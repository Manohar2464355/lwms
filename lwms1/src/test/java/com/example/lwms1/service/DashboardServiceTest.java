package com.example.lwms1.service;

import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.Shipment;
import com.example.lwms1.model.Space;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    void setUp() {
        // Use lenient() to prevent UnnecessaryStubbingException
        // These provide defaults so getAllStats() doesn't throw NullPointer
        lenient().when(inventoryService.listAll()).thenReturn(Collections.emptyList());
        lenient().when(shipmentService.listAll()).thenReturn(Collections.emptyList());
        lenient().when(maintenanceService.getPendingMaintenanceCount()).thenReturn(0L);
        lenient().when(reportService.listAll()).thenReturn(Collections.emptyList());
        lenient().when(spaceService.listAll()).thenReturn(Collections.emptyList());
        lenient().when(userService.listAll()).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("Stats: Should calculate 50.0% utilization")
    void testUtilizationCalculation() {
        // Arrange
        Space s1 = new Space();
        s1.setUsedCapacity(50); // Ensure Space model has these setters
        s1.setTotalCapacity(100);

        when(spaceService.listAll()).thenReturn(Collections.singletonList(s1));

        // Act
        Map<String, Object> stats = dashboardService.getAllStats();

        // Assert
        assertEquals("50.0%", stats.get("warehouseUtilization"));
    }

    @Test
    @DisplayName("Stats: Basic Counts verification")
    void testBasicCounts() {
        // Arrange
        Inventory item = new Inventory();
        item.setItemName("Test Item");

        Shipment ship = new Shipment();
        ship.setOrigin("Warehouse A");

        when(inventoryService.listAll()).thenReturn(Arrays.asList(item));
        when(shipmentService.listAll()).thenReturn(Arrays.asList(ship));

        // Act
        Map<String, Object> stats = dashboardService.getAllStats();

        // Assert
        assertEquals(1, stats.get("inventoryCount"));
        assertEquals(1, stats.get("activeShipmentsCount"));
    }
}