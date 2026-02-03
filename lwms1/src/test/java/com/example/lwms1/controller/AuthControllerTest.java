package com.example.lwms1.controller;

import com.example.lwms1.dto.UserCreateDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private Model model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    @DisplayName("Login Page: Should return view name with no parameters")
    void showLoginPage_Success() {
        // ACT: Calling it with () because your controller has no params
        String viewName = authController.login();

        // ASSERT
        assertEquals("admin/auth/login", viewName);
    }

    @Test
    @DisplayName("Register Page: Should add userDto and return register view")
    void showRegistrationForm_Success() {
        // ACT: Calling it with (model) because your controller needs it
        String viewName = authController.showRegistrationForm(model);

        // ASSERT
        assertEquals("admin/auth/register", viewName);
        assertTrue(model.containsAttribute("userDto"));
    }

    @Test
    @DisplayName("Register Post: Should redirect to login on success")
    void registerUser_Success() {
        // ARRANGE
        UserCreateDTO dto = new UserCreateDTO();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        // ACT
        String viewName = authController.registerUser(dto, result, redirectAttributes);

        // ASSERT
        assertEquals("redirect:/login", viewName);
        verify(userService).createUser(dto);
    }

    @Test
    @DisplayName("Register Post: Should return register view if validation fails")
    void registerUser_ValidationError() {
        // ARRANGE
        UserCreateDTO dto = new UserCreateDTO();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);

        // ACT
        String viewName = authController.registerUser(dto, result, redirectAttributes);

        // ASSERT
        assertEquals("admin/auth/register", viewName);
        verify(userService, never()).createUser(any());
    }
}