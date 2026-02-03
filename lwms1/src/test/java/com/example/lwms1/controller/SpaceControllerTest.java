package com.example.lwms1.controller;

import com.example.lwms1.dto.SpaceDTO;
import com.example.lwms1.model.Space;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceControllerTest {

    @Mock
    private SpaceService spaceService;

    @InjectMocks
    private SpaceController spaceController;

    private Model model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    @DisplayName("Usage Page: Should return usage view and populate space list")
    void testUsagePageLoads() {
        // Arrange
        when(spaceService.listAll()).thenReturn(Collections.emptyList());

        // Act
        String viewName = spaceController.usage(model);

        // Assert
        assertEquals("admin/space/usage", viewName);
        assertTrue(model.containsAttribute("spaces"));
        assertTrue(model.containsAttribute("form"));
        verify(spaceService).listAll();
    }

    @Test
    @DisplayName("Add Space: Should redirect and show success message")
    void testAddSpaceSuccess() {
        // Arrange
        SpaceDTO dto = new SpaceDTO();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        // Act
        String viewName = spaceController.add(dto, result, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/space", viewName);
        verify(spaceService).create(dto);
        assertEquals("Zone created successfully!", redirectAttributes.getFlashAttributes().get("success"));
    }

    @Test
    @DisplayName("Add Space: Should return usage view on validation failure")
    void testAddSpaceValidationFailure() {
        // Arrange
        SpaceDTO dto = new SpaceDTO();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);
        when(spaceService.listAll()).thenReturn(Collections.emptyList());

        // Act
        String viewName = spaceController.add(dto, result, model, redirectAttributes);

        // Assert
        assertEquals("admin/space/usage", viewName);
        verify(spaceService, never()).create(any());
    }

    @Test
    @DisplayName("Edit Page: Should map Space entity to DTO and return edit view")
    void testEditPageLoads() {
        // Arrange
        Space mockSpace = new Space();
        mockSpace.setZone("Zone B");
        mockSpace.setTotalCapacity(50);
        when(spaceService.getById(1)).thenReturn(mockSpace);

        // Act
        String viewName = spaceController.edit(1, model);

        // Assert
        assertEquals("admin/space/edit", viewName);
        assertEquals(1, model.getAttribute("spaceId"));
        SpaceDTO form = (SpaceDTO) model.getAttribute("form");
        assertEquals("Zone B", form.getZone());
    }

    @Test
    @DisplayName("Update: Should call service update and redirect")
    void testUpdateSuccess() {
        // Arrange
        SpaceDTO dto = new SpaceDTO();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        // Act
        String viewName = spaceController.update(1, dto, result, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/space", viewName);
        verify(spaceService).update(eq(1), any(SpaceDTO.class));
        assertEquals("Zone updated successfully!", redirectAttributes.getFlashAttributes().get("success"));
    }

    @Test
    @DisplayName("Delete: Should call service delete and redirect")
    void testDeleteSpace() {
        // Act
        String viewName = spaceController.delete(1, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/space", viewName);
        verify(spaceService).delete(1);
    }
}