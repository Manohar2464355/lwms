package com.example.lwms1.controller;

import com.example.lwms1.dto.InventoryDTO;
import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.MaintenanceSchedule;
import com.example.lwms1.repository.MaintenanceScheduleRepository;
import com.example.lwms1.service.InventoryService;
import com.example.lwms1.service.SpaceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private InventoryService inventoryService;
    @MockBean private SpaceService spaceService;
    @MockBean private MaintenanceScheduleRepository maintenanceRepo;

    @Test
    @DisplayName("List inventory should show items and identify locked spaces")
    @WithMockUser(roles = "ADMIN")
    void listInventory_Success() throws Exception {
        // Mocking maintenance schedule to simulate a locked space (ID: 101)
        MaintenanceSchedule schedule = new MaintenanceSchedule();
        schedule.setEquipmentId(101);
        schedule.setCompletionStatus("PENDING");

        when(maintenanceRepo.findAll()).thenReturn(List.of(schedule));
        when(inventoryService.listAll()).thenReturn(Collections.emptyList());
        when(spaceService.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/inventory"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/inventory/list"))
                .andExpect(model().attribute("lockedSpaceIds", List.of(101)));
    }

    @Test
    @DisplayName("Add item should redirect on success")
    @WithMockUser(roles = "ADMIN")
    void addItem_Success() throws Exception {
        mockMvc.perform(post("/inventory/add")
                        .param("itemName", "Pallet Jack")
                        .param("category", "Equipment")
                        .param("quantity", "5")
                        .param("location", "Zone A")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(inventoryService, times(1)).create(any(InventoryDTO.class));
    }

    @Test
    @DisplayName("Update item should redirect on success")
    @WithMockUser(roles = "ADMIN")
    void updateItem_Success() throws Exception {
        mockMvc.perform(post("/inventory/update/1")
                        .param("itemName", "Updated Item")
                        .param("category", "Tools")
                        .param("quantity", "10")
                        .param("location", "Zone B")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory"));

        verify(inventoryService).update(eq(1), any(InventoryDTO.class));
    }

    @Test
    @DisplayName("Delete item should redirect")
    @WithMockUser(roles = "ADMIN")
    void deleteItem_Success() throws Exception {
        mockMvc.perform(get("/inventory/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inventory"));

        verify(inventoryService).delete(1);
    }

    @Test
    @DisplayName("Show edit form should load item data into DTO")
    @WithMockUser(roles = "ADMIN")
    void showEditForm_Success() throws Exception {
        Inventory item = new Inventory();
        item.setItemId(1);
        item.setItemName("Forklift");
        item.setQuantity(2);

        when(inventoryService.findById(1)).thenReturn(item);

        mockMvc.perform(get("/inventory/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/inventory/edit"))
                .andExpect(model().attributeExists("inventoryDTO"));
    }
}