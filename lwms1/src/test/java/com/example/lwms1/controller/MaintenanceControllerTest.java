package com.example.lwms1.controller;

import com.example.lwms1.dto.MaintenanceDTO;
import com.example.lwms1.service.MaintenanceService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceControllerTest {

    @Mock
    private MaintenanceService maintenanceService;

    @Mock
    private SpaceService spaceService;

    @InjectMocks
    private MaintenanceController maintenanceController;

    private Model model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    @DisplayName("List: Should return view and populate maintenance schedules")
    void list_Success() {
        // Arrange
        when(maintenanceService.listAll()).thenReturn(Collections.emptyList());
        when(spaceService.listAll()).thenReturn(Collections.emptyList());

        // Act
        String viewName = maintenanceController.list(model);

        // Assert
        assertEquals("admin/maintenance/schedule", viewName);
        verify(maintenanceService).listAll();
        verify(spaceService).listAll();
    }

    @Test
    @DisplayName("Schedule: Should redirect and show lock message on successful post")
    void schedule_Success() {
        // Arrange
        MaintenanceDTO dto = new MaintenanceDTO();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        // Act
        String viewName = maintenanceController.schedule(dto, result, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/maintenance", viewName);
        verify(maintenanceService).schedule(dto);
        assertEquals("Maintenance scheduled! Zone is now locked.",
                redirectAttributes.getFlashAttributes().get("successMessage"));
    }

    @Test
    @DisplayName("Toggle: Should show UNLOCKED message when status becomes COMPLETED")
    void toggleStatus_Completed() {
        // Arrange
        when(maintenanceService.toggleStatus(1)).thenReturn("COMPLETED");

        // Act
        String viewName = maintenanceController.toggleStatus(1, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/maintenance", viewName);
        assertEquals("Maintenance finished. Zone is UNLOCKED.",
                redirectAttributes.getFlashAttributes().get("successMessage"));
    }

    @Test
    @DisplayName("Toggle: Should show LOCKED message when status is NOT COMPLETED")
    void toggleStatus_Pending() {
        // Arrange
        when(maintenanceService.toggleStatus(1)).thenReturn("PENDING");

        // Act
        String viewName = maintenanceController.toggleStatus(1, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/maintenance", viewName);
        assertEquals("Maintenance reopened. Zone is LOCKED.",
                redirectAttributes.getFlashAttributes().get("successMessage"));
    }

    @Test
    @DisplayName("Update: Should call service and redirect on success")
    void update_Success() {
        // Arrange
        MaintenanceDTO dto = new MaintenanceDTO();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        // Act
        String viewName = maintenanceController.update(1, dto, result, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/maintenance", viewName);
        verify(maintenanceService).update(eq(1), any(MaintenanceDTO.class));
    }

    @Test
    @DisplayName("Delete: Should call service and redirect")
    void delete_Success() {
        // Act
        String viewName = maintenanceController.delete(1, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/maintenance", viewName);
        verify(maintenanceService).delete(1);
    }
}