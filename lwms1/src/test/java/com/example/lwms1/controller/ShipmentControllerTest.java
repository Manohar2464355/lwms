package com.example.lwms1.controller;

import com.example.lwms1.dto.ShipmentDTO;
import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.Shipment;
import com.example.lwms1.service.InventoryService;
import com.example.lwms1.service.ShipmentService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentControllerTest {

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ShipmentController shipmentController;

    private Model model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    @DisplayName("List: Should return view and populate shipments/items")
    void list_Success() {
        // Arrange
        when(shipmentService.listAll()).thenReturn(Collections.emptyList());
        when(inventoryService.listAll()).thenReturn(Collections.emptyList());

        // Act
        String viewName = shipmentController.list(model);

        // Assert
        assertEquals("admin/shipment/list", viewName);
        assertTrue(model.containsAttribute("shipments"));
        assertTrue(model.containsAttribute("items"));
        assertTrue(model.containsAttribute("form"));
    }

    @Test
    @DisplayName("Receive: Should call service and redirect with success flash message")
    void receive_Success() {
        // Arrange
        ShipmentDTO dto = new ShipmentDTO();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        // Act
        String viewName = shipmentController.receive(dto, result, redirectAttributes, model);

        // Assert
        assertEquals("redirect:/admin/shipments", viewName);
        verify(shipmentService).receive(dto);
        assertEquals("New shipment registered!", redirectAttributes.getFlashAttributes().get("success"));
    }

    @Test
    @DisplayName("Update: Should call updateFullShipment and redirect")
    void update_Success() {
        // Arrange
        ShipmentDTO dto = new ShipmentDTO();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        // Act
        String viewName = shipmentController.update(dto, result, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/shipments", viewName);
        verify(shipmentService).updateFullShipment(dto);
    }

    @Test
    @DisplayName("Track: Should return track view with shipment data")
    void track_Success() {
        // Arrange
        Shipment s = new Shipment();
        s.setShipmentId(10);
        when(shipmentService.get(10)).thenReturn(s);

        // Act
        String viewName = shipmentController.track(10, model);

        // Assert
        assertEquals("admin/shipment/track", viewName);
        assertEquals(s, model.getAttribute("shipment"));
    }

    @Test
    @DisplayName("Edit Form: Should map Entity to DTO and return edit view")
    void showEditForm_Success() {
        // Arrange
        Shipment s = new Shipment();
        s.setShipmentId(10);
        s.setOrigin("London");
        Inventory i = new Inventory();
        i.setItemId(1);
        s.setInventory(i);

        when(shipmentService.get(10)).thenReturn(s);
        when(inventoryService.listAll()).thenReturn(Collections.emptyList());

        // Act
        String viewName = shipmentController.showEditForm(10, model);

        // Assert
        assertEquals("admin/shipment/edit", viewName);
        ShipmentDTO form = (ShipmentDTO) model.getAttribute("form");
        assertEquals("London", form.getOrigin());
        assertEquals(1, form.getItemId());
    }

    @Test
    @DisplayName("Delete: Should call service and redirect")
    void delete_Success() {
        // Act
        String viewName = shipmentController.delete(10, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/shipments", viewName);
        verify(shipmentService).delete(10);
        assertEquals("Shipment record removed.", redirectAttributes.getFlashAttributes().get("success"));
    }
}