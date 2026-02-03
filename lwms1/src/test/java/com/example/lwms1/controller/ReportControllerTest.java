package com.example.lwms1.controller;

import com.example.lwms1.dto.ReportDTO;
import com.example.lwms1.service.ReportService;
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
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    @DisplayName("Report list should be accessible by Admin")
    @WithMockUser(roles = "ADMIN")
    void list_AdminAccess() throws Exception {
        when(reportService.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/report/list"))
                .andExpect(model().attributeExists("reports", "form"));
    }

    @Test
    @DisplayName("Generate report should redirect on success")
    @WithMockUser(roles = "ADMIN")
    void generateReport_Success() throws Exception {
        mockMvc.perform(post("/admin/reports/generate")
                        .param("reportType", "INVENTORY_SUMMARY")
                        .param("details", "Monthly inventory check")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reports"))
                .andExpect(flash().attribute("successMessage", "Report for INVENTORY_SUMMARY generated successfully!"));

        verify(reportService, times(1)).generate(any(ReportDTO.class));
    }

    @Test
    @DisplayName("Delete report should redirect on success")
    @WithMockUser(roles = "ADMIN")
    void deleteReport_Success() throws Exception {
        mockMvc.perform(post("/admin/reports/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reports"))
                .andExpect(flash().attribute("successMessage", "Report deleted."));

        verify(reportService, times(1)).delete(1);
    }

    @Test
    @DisplayName("Reports should be forbidden for regular users")
    @WithMockUser(roles = "USER")
    void list_UserForbidden() throws Exception {
        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isForbidden());
    }
}