package com.example.lwms1.controller;

import com.example.lwms1.dto.UserCreateDTO;
import com.example.lwms1.dto.UserRoleUpdateDTO;
import com.example.lwms1.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // We must mock ALL services injected into the controller
    @MockBean private UserService userService;
    @MockBean private InventoryService inventoryService;
    @MockBean private ShipmentService shipmentService;
    @MockBean private MaintenanceService maintenanceService;
    @MockBean private ReportService reportService;
    @MockBean private SpaceService spaceService;
    @MockBean private DashboardService dashboardService;

    // --- SECTION 1: SECURITY & ACCESS ---

    @Test
    @DisplayName("Dashboard should be accessible by Admin")
    @WithMockUser(roles = "ADMIN")
    void dashboard_AdminAccess() throws Exception {
        when(dashboardService.getAllStats()).thenReturn(Map.of("totalUsers", 5));

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("totalUsers"));
    }

    @Test
    @DisplayName("Dashboard should be forbidden for regular User")
    @WithMockUser(roles = "USER")
    void dashboard_UserAccessForbidden() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden()); // 403
    }

    // --- SECTION 2: USER MANAGEMENT ---

    @Test
    @DisplayName("List users should return user list and forms")
    @WithMockUser(roles = "ADMIN")
    void listUsers_Success() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users", "createForm", "roleForm"))
                .andExpect(view().name("admin/users/list"));
    }

    @Test
    @DisplayName("Create user success should redirect")
    @WithMockUser(roles = "ADMIN")
    void createUser_Success() throws Exception {
        mockMvc.perform(post("/admin/users/create")
                        .param("username", "newAdmin")
                        .param("email", "admin@lwms.com")
                        .param("password", "securePass123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attribute("success", "User created successfully!"));

        verify(userService, times(1)).createUser(any(UserCreateDTO.class));
    }

    @Test
    @DisplayName("Set role success should redirect")
    @WithMockUser(roles = "ADMIN")
    void setRole_Success() throws Exception {
        mockMvc.perform(post("/admin/users/set-role")
                        .param("username", "testuser")
                        .param("role", "STAFF")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, times(1)).setUserRole(any(UserRoleUpdateDTO.class));
    }

    @Test
    @DisplayName("Delete user should call service and redirect")
    @WithMockUser(roles = "ADMIN")
    void deleteUser_Success() throws Exception {
        mockMvc.perform(post("/admin/users/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attribute("success", "User deleted."));

        verify(userService).deleteUser(1L);
    }
}