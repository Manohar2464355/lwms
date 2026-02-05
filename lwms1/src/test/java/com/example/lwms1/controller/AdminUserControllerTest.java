package com.example.lwms1.controller;

import com.example.lwms1.dto.UserCreateDTO;
import com.example.lwms1.service.DashboardService;
import com.example.lwms1.service.UserService;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private AdminUserController adminUserController;

    private Model model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    @DisplayName("Dashboard: Should return view and populate stats")
    void adminDashboard_Success() {
        Map<String, Object> mockStats = Map.of("inventoryCount", 5);
        when(dashboardService.getAllStats()).thenReturn(mockStats);
        String viewName = adminUserController.adminDashboard(model);
        assertEquals("admin/dashboard", viewName);
        assertEquals(5, model.getAttribute("inventoryCount"));
        verify(dashboardService).getAllStats();
    }

    @Test
    @DisplayName("List Users: Should add users and forms to model")
    void listUsers_Success() {
        when(userService.listAll()).thenReturn(Collections.emptyList());
        String viewName = adminUserController.listUsers(model);
        assertEquals("admin/users/list", viewName);
        assertEquals(Collections.emptyList(), model.getAttribute("users"));
        verify(userService).listAll();
    }

    @Test
    @DisplayName("Create User: Success should redirect to list")
    void createUser_Success() {
        UserCreateDTO dto = new UserCreateDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        String viewName = adminUserController.create(dto, bindingResult, model, redirectAttributes);
        assertEquals("redirect:/admin/users", viewName);
        verify(userService).createUser(dto);
    }

    @Test
    @DisplayName("Create User: Validation failure should return list view")
    void createUser_ValidationFailure() {
        UserCreateDTO dto = new UserCreateDTO();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(userService.listAll()).thenReturn(Collections.emptyList());
        String viewName = adminUserController.create(dto, bindingResult, model, redirectAttributes);
        assertEquals("admin/users/list", viewName);
        verify(userService, never()).createUser(any());
    }

    @Test
    @DisplayName("Delete User: Should call service and redirect")
    void deleteUser_Success() {
        String viewName = adminUserController.delete(1L, redirectAttributes);
        assertEquals("redirect:/admin/users", viewName);
        verify(userService).deleteUser(1L);
    }
}