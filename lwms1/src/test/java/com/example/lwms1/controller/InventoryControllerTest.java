package com.example.lwms1.controller;

import com.example.lwms1.dto.InventoryDTO;
import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.MaintenanceSchedule;
import com.example.lwms1.repository.MaintenanceScheduleRepository;
import com.example.lwms1.service.InventoryService;
import com.example.lwms1.service.SpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock private InventoryService inventoryService;
    @Mock private SpaceService spaceService;
    @Mock private MaintenanceScheduleRepository maintenanceRepo;

    @InjectMocks
    private InventoryController inventoryController;

    private Model model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    @DisplayName("List: Should return view and identify 'PENDING' maintenance as locked spaces")
    void listInventory_Success() {
        // Arrange
        MaintenanceSchedule schedule = new MaintenanceSchedule();
        schedule.setEquipmentId(101);
        schedule.setCompletionStatus("PENDING"); // Logic from your controller

        when(maintenanceRepo.findAll()).thenReturn(List.of(schedule));
        when(inventoryService.listAll()).thenReturn(Collections.emptyList());
        when(spaceService.listAll()).thenReturn(Collections.emptyList());

        // Act
        String viewName = inventoryController.listInventory(model);

        // Assert
        assertEquals("admin/inventory/list", viewName);
        List<Integer> lockedIds = (List<Integer>) model.getAttribute("lockedSpaceIds");
        assertTrue(lockedIds.contains(101), "Space 101 should be locked");
    }

    @Test
    @DisplayName("Add Item: Should redirect to /inventory on success")
    void addItem_Success() {
        // Arrange
        InventoryDTO dto = new InventoryDTO();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        // Act
        String viewName = inventoryController.addItem(dto, result, redirectAttributes, model);

        // Assert
        assertEquals("redirect:/inventory", viewName);
        verify(inventoryService).create(dto);
        assertEquals("Item added successfully!", redirectAttributes.getFlashAttributes().get("successMessage"));
    }

    @Test
    @DisplayName("Update Item: Should call service update with Integer ID")
    void updateItem_Success() {
        // Arrange
        InventoryDTO dto = new InventoryDTO();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        // Act
        String viewName = inventoryController.updateItem(1, dto, result, redirectAttributes);

        // Assert
        assertEquals("redirect:/inventory", viewName);
        verify(inventoryService).update(eq(1), any(InventoryDTO.class));
    }

    @Test
    @DisplayName("Delete Item: Should call service delete and redirect")
    void deleteItem_Success() {
        // Act
        String viewName = inventoryController.deleteItem(1, redirectAttributes);

        // Assert
        assertEquals("redirect:/inventory", viewName);
        verify(inventoryService).delete(1);
    }

    @Test
    @DisplayName("Edit Form: Should map Inventory model to DTO for the view")
    void showEditForm_Success() {
        // Arrange
        Inventory item = new Inventory();
        item.setItemId(50);
        item.setItemName("Test Item");
        when(inventoryService.findById(50)).thenReturn(item);

        // Act
        String viewName = inventoryController.showEditForm(50, model);

        // Assert
        assertEquals("admin/inventory/edit", viewName);
        InventoryDTO resultDto = (InventoryDTO) model.getAttribute("inventoryDTO");
        assertEquals(50, resultDto.getItemId());
        assertEquals("Test Item", resultDto.getItemName());
    }
}