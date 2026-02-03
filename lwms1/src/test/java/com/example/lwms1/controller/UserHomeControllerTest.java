package com.example.lwms1.controller;

import com.example.lwms1.model.Shipment;
import com.example.lwms1.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserHomeControllerTest {

    @Mock private InventoryService inventoryService;
    @Mock private ShipmentService shipmentService;
    @Mock private SpaceService spaceService;
    @Mock private MaintenanceService maintenanceService;
    @Mock private ReportService reportService;
    @Mock private DashboardService dashboardService;
    @Mock private Authentication auth;

    @InjectMocks
    private UserHomeController userHomeController;

    private Model model;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
    }

    @Test
    @DisplayName("User Home: Should add stats and username to model")
    void testUserHome() {
        // Arrange
        when(auth.getName()).thenReturn("testUser");
        Map<String, Object> stats = Map.of("totalInventory", 100, "activeShipments", 5);
        when(dashboardService.getAllStats()).thenReturn(stats);

        // Act
        String viewName = userHomeController.userHome(auth, model);

        // Assert
        assertEquals("user/home", viewName);
        assertEquals("testUser", model.getAttribute("username"));
        assertEquals(100, model.getAttribute("totalInventory"));
        verify(dashboardService).getAllStats();
    }

    @Test
    @DisplayName("Inventory: Should return user inventory view")
    void testViewInventory() {
        when(inventoryService.listAll()).thenReturn(Collections.emptyList());

        String viewName = userHomeController.viewInventory(model);

        assertEquals("user/inventory", viewName);
        assertTrue(model.containsAttribute("items"));
    }

    @Test
    @DisplayName("Shipments: Should return shipment view")
    void testViewShipments() {
        when(shipmentService.listAll()).thenReturn(Collections.emptyList());

        String viewName = userHomeController.viewShipments(model);

        assertEquals("user/shipment", viewName);
        assertTrue(model.containsAttribute("shipmentList"));
    }

    @Test
    @DisplayName("Profile: Should populate username and roles from Authentication")
    void testViewProfile() {
        // Arrange
        when(auth.getName()).thenReturn("worker1");
        when(auth.getAuthorities()).thenReturn(Collections.emptyList());

        // Act
        String viewName = userHomeController.viewProfile(auth, model);

        // Assert
        assertEquals("user/profile", viewName);
        assertEquals("worker1", model.getAttribute("username"));
        assertTrue(model.containsAttribute("roles"));
    }

    @Test
    @DisplayName("Track Shipment: Should retrieve specific shipment by ID")
    void testTrackShipment() {
        // Arrange
        Integer shipmentId = 101;
        Shipment mockShipment = new Shipment();
        mockShipment.setShipmentId(shipmentId);
        when(shipmentService.get(shipmentId)).thenReturn(mockShipment);

        // Act
        String viewName = userHomeController.trackShipment(shipmentId, model);

        // Assert
        assertEquals("user/track", viewName);
        assertEquals(mockShipment, model.getAttribute("shipment"));
    }
}