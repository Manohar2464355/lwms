package com.example.lwms1.controller;

import com.example.lwms1.dto.ShipmentDTO;
import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.Shipment;
import com.example.lwms1.service.InventoryService;
import com.example.lwms1.service.ShipmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShipmentService shipmentService;

    @MockBean
    private InventoryService inventoryService;

    @Test
    @DisplayName("Shipment list should load shipments and inventory items")
    @WithMockUser(roles = "ADMIN")
    void list_Success() throws Exception {
        when(shipmentService.listAll()).thenReturn(Collections.emptyList());
        when(inventoryService.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/shipments"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/shipment/list"))
                .andExpect(model().attributeExists("shipments", "items", "form"));
    }

    @Test
    @DisplayName("Receive shipment should redirect on success")
    @WithMockUser(roles = "ADMIN")
    void receive_Success() throws Exception {
        mockMvc.perform(post("/admin/shipments/receive")
                        .param("itemId", "1")
                        .param("origin", "Warehouse A")
                        .param("destination", "Store B")
                        .param("status", "IN_TRANSIT")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/shipments"))
                .andExpect(flash().attribute("success", "New shipment registered!"));

        verify(shipmentService, times(1)).receive(any(ShipmentDTO.class));
    }

    @Test
    @DisplayName("Update shipment should redirect on success")
    @WithMockUser(roles = "ADMIN")
    void update_Success() throws Exception {
        mockMvc.perform(post("/admin/shipments/update")
                        .param("shipmentId", "10")
                        .param("itemId", "1")
                        .param("origin", "Updated Origin")
                        .param("destination", "New York Hub") // ADD THIS LINE
                        .param("status", "DELIVERED")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/shipments"));

        verify(shipmentService, times(1)).updateFullShipment(any(ShipmentDTO.class));
    }

    @Test
    @DisplayName("Tracking should be accessible by both ADMIN and USER roles")
    @WithMockUser(roles = "USER") // Testing that even a standard user can track
    void track_AccessSuccess() throws Exception {
        Shipment mockShipment = new Shipment();
        mockShipment.setShipmentId(10);
        when(shipmentService.get(10)).thenReturn(mockShipment);

        mockMvc.perform(get("/admin/shipments/track/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/shipment/track"))
                .andExpect(model().attributeExists("shipment"));
    }

    @Test
    @DisplayName("Edit form should map shipment data to DTO")
    @WithMockUser(roles = "ADMIN")
    void showEditForm_Success() throws Exception {
        Shipment s = new Shipment();
        s.setShipmentId(10);
        s.setOrigin("London");

        Inventory i = new Inventory();
        i.setItemId(1);
        s.setInventory(i);

        when(shipmentService.get(10)).thenReturn(s);

        mockMvc.perform(get("/admin/shipments/edit/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/shipment/edit"))
                .andExpect(model().attributeExists("form", "items"));
    }

    @Test
    @DisplayName("Delete shipment should redirect")
    @WithMockUser(roles = "ADMIN")
    void delete_Success() throws Exception {
        mockMvc.perform(post("/admin/shipments/delete/10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/shipments"));

        verify(shipmentService).delete(10);
    }
}