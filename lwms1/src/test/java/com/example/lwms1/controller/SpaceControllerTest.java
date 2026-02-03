package com.example.lwms1.controller;

import com.example.lwms1.dto.SpaceDTO;
import com.example.lwms1.service.SpaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(SpaceController.class)
@WithMockUser(roles = "ADMIN")
public class SpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpaceService spaceService;

    @Test
    void testUsagePageLoads() throws Exception {
        when(spaceService.listAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/space"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/space/usage"))
                // Removed "alloc" as it is no longer used in the controller
                .andExpect(model().attributeExists("spaces", "form"));
    }

    @Test
    void testAddSpaceSuccess() throws Exception {
        mockMvc.perform(post("/admin/space/add")
                        .param("zone", "Zone A")
                        .param("totalCapacity", "100")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/space"))
                .andExpect(flash().attributeExists("success"));

        verify(spaceService, times(1)).create(any(SpaceDTO.class));
    }

    @Test
    void testAddSpaceValidationFailure() throws Exception {
        mockMvc.perform(post("/admin/space/add")
                        .param("zone", "")
                        .param("totalCapacity", "-5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/space/usage"))
                .andExpect(model().hasErrors());

        verify(spaceService, never()).create(any());
    }

    @Test
    void testEditPageLoads() throws Exception {
        var mockSpace = new com.example.lwms1.model.Space();
        mockSpace.setZone("Zone B");
        mockSpace.setTotalCapacity(50);

        when(spaceService.getById(1)).thenReturn(mockSpace);

        mockMvc.perform(get("/admin/space/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/space/edit"))
                .andExpect(model().attribute("spaceId", 1))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    void testUpdateSuccess() throws Exception {
        mockMvc.perform(post("/admin/space/update/1")
                        .param("zone", "Updated Zone")
                        .param("totalCapacity", "200")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/space"))
                .andExpect(flash().attribute("success", "Zone updated successfully!"));

        verify(spaceService).update(eq(1), any(SpaceDTO.class));
    }

    @Test
    void testDeleteSpace() throws Exception {
        mockMvc.perform(post("/admin/space/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/space"));

        verify(spaceService).delete(1);
    }
}