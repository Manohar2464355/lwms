package com.example.lwms1.controller;

import com.example.lwms1.dto.MaintenanceDTO;
import com.example.lwms1.service.MaintenanceService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MaintenanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MaintenanceService maintenanceService;

    @MockBean
    private SpaceService spaceService;

    @Test
    @DisplayName("Maintenance list should be accessible by Admin")
    @WithMockUser(roles = "ADMIN")
    void list_AdminAccess() throws Exception {
        when(maintenanceService.listAll()).thenReturn(Collections.emptyList());
        when(spaceService.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/maintenance"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/maintenance/schedule"))
                .andExpect(model().attributeExists("schedules", "availableSpaces", "form"));
    }

    @Test
    @DisplayName("Schedule task success should redirect")
    @WithMockUser(roles = "ADMIN")
    void schedule_Success() throws Exception {
        mockMvc.perform(post("/admin/maintenance/schedule")
                        .param("equipmentId", "101")
                        .param("description", "Routine Check")
                        .param("scheduledDate", "2026-12-31")
                        .param("completionStatus", "PENDING")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/maintenance"))
                .andExpect(flash().attribute("successMessage", "Maintenance task scheduled successfully! Zone is now locked."));

        verify(maintenanceService, times(1)).schedule(any(MaintenanceDTO.class));
    }

    @Test
    @DisplayName("Toggle status to COMPLETED should show unlock message")
    @WithMockUser(roles = "ADMIN")
    void toggleStatus_ToCompleted() throws Exception {
        when(maintenanceService.toggleStatus(1)).thenReturn("COMPLETED");

        mockMvc.perform(post("/admin/maintenance/toggle/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Maintenance completed. Zone is now UNLOCKED."));
    }

    @Test
    @DisplayName("Delete maintenance task should redirect")
    @WithMockUser(roles = "ADMIN")
    void delete_Success() throws Exception {
        mockMvc.perform(post("/admin/maintenance/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/maintenance"));

        verify(maintenanceService).delete(1);
    }

    @Test
    @DisplayName("Maintenance access should be forbidden for regular USER")
    @WithMockUser(roles = "USER")
    void list_ForbiddenForUser() throws Exception {
        mockMvc.perform(get("/admin/maintenance"))
                .andExpect(status().isForbidden());
    }
}