package com.example.lwms1.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CustomErrorControllerTest {

    @InjectMocks
    private CustomErrorController customErrorController;

    @Test
    @DisplayName("403 Error: Should return the correct access denied view name")
    void testAccessDeniedView() {
        // ACT
        // Note: Check your Controller method name.
        // If it's named 'accessDenied', call that here.
        String viewName = customErrorController.accessDenied();

        // ASSERT
        assertEquals("admin/error/403", viewName);
    }
}