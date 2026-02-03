package com.example.lwms1.controller;

import com.example.lwms1.dto.ReportDTO;
import com.example.lwms1.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private Model model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    @DisplayName("List: Should return report list view and populate model")
    void list_Success() {
        // Arrange
        when(reportService.listAll()).thenReturn(Collections.emptyList());

        // Act
        String viewName = reportController.list(model);

        // Assert
        assertEquals("admin/report/list", viewName);
        assertTrue(model.containsAttribute("reports"));
        assertTrue(model.containsAttribute("form"));
        verify(reportService).listAll();
    }

    @Test
    @DisplayName("Generate: Should call service and redirect with dynamic success message")
    void generate_Success() {
        // Arrange
        ReportDTO dto = new ReportDTO();
        dto.setReportType("INVENTORY_SUMMARY");

        // Act
        String viewName = reportController.generate(dto, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/reports", viewName);
        verify(reportService).generate(dto);
        assertEquals("Report for INVENTORY_SUMMARY generated!",
                redirectAttributes.getFlashAttributes().get("successMessage"));
    }

    @Test
    @DisplayName("Delete: Should call service delete with Integer ID and redirect")
    void delete_Success() {
        // Act
        String viewName = reportController.delete(1, redirectAttributes);

        // Assert
        assertEquals("redirect:/admin/reports", viewName);
        verify(reportService).delete(1);
        assertEquals("Report deleted successfully.",
                redirectAttributes.getFlashAttributes().get("successMessage"));
    }
}