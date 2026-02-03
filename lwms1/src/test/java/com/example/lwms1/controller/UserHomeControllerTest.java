package com.example.lwms1.controller;

import com.example.lwms1.model.Shipment; // Ensure this import matches your project structure
import com.example.lwms1.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserHomeController.class)
public class UserHomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private InventoryService inventoryService;
    @MockBean private ShipmentService shipmentService;
    @MockBean private SpaceService spaceService;
    @MockBean private MaintenanceService maintenanceService;
    @MockBean private ReportService reportService;
    @MockBean private DashboardService dashboardService;

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testUserHome() throws Exception {
        Map<String, Object> stats = Map.of("totalInventory", 100, "activeShipments", 5);
        when(dashboardService.getAllStats()).thenReturn(stats);

        mockMvc.perform(get("/user/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/home"))
                .andExpect(model().attribute("username", "testUser"))
                .andExpect(model().attribute("totalInventory", 100));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testViewInventory() throws Exception {
        when(inventoryService.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/inventory"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/inventory"))
                .andExpect(model().attributeExists("items"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testViewShipments() throws Exception {
        when(shipmentService.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/shipments"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/shipment"))
                .andExpect(model().attributeExists("shipmentList"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testViewSpace() throws Exception {
        when(spaceService.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/space"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/space"))
                .andExpect(model().attributeExists("spaces"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testViewMaintenance() throws Exception {
        when(maintenanceService.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/maintenance"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/maintenance"))
                .andExpect(model().attributeExists("schedules"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testViewReports() throws Exception {
        when(reportService.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/reports"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/reports"))
                .andExpect(model().attributeExists("reports"));
    }

    @Test
    @WithMockUser(username = "worker1", roles = "USER")
    void testViewProfile() throws Exception {
        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile"))
                .andExpect(model().attribute("username", "worker1"))
                .andExpect(model().attributeExists("roles"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testTrackShipment() throws Exception {
        Integer shipmentId = 101;

        // FIX: Create a real instance of your model instead of new Object()
        Shipment mockShipment = new Shipment();
        mockShipment.setShipmentId(shipmentId);

        when(shipmentService.get(shipmentId)).thenReturn(mockShipment);

        mockMvc.perform(get("/user/shipments/track/{id}", shipmentId))
                .andExpect(status().isOk())
                .andExpect(view().name("user/track"))
                .andExpect(model().attributeExists("shipment"))
                .andExpect(model().attribute("shipment", mockShipment));
    }

    @Test
    void testAccessDeniedForUnauthenticated() throws Exception {
        mockMvc.perform(get("/user/home"))
                // In many test environments, this defaults to 401
                .andExpect(status().isUnauthorized());
    }
}