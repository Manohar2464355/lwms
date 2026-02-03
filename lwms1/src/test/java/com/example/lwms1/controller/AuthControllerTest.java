package com.example.lwms1.controller; // Ensure this matches your actual folder path

// 1. Project-specific imports (Fixes red text on DTO and Service)
import com.example.lwms1.dto.UserCreateDTO;
import com.example.lwms1.service.UserService;

// 2. JUnit and Spring Test imports
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

// 3. Static imports for MockMvc and Mockito (Fixes red text on status(), get(), etc.)
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Root URL should redirect anonymous user to login")
    void rootRedirect_AnonymousUser() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Admin user should redirect to admin dashboard from root")
    void rootRedirect_AdminUser() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Standard user should redirect to user home from root")
    void rootRedirect_StandardUser() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/home"));
    }

    @Test
    @DisplayName("Login page should be accessible")
    void showLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/auth/login"));
    }

    @Test
    @DisplayName("Registration form should initialize UserCreateDTO")
    void showRegistrationForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/auth/register"))
                .andExpect(model().attributeExists("userDto"));
    }

    @Test
    @DisplayName("Successful registration should call service and redirect")
    void registerUser_Success() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "testuser")
                        .param("password", "password123")
                        .param("email", "test@example.com") // <--- ADD THIS LINE
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService, times(1)).createUser(any(UserCreateDTO.class));
    }

    @Test
    @DisplayName("Validation failure should return register view")
    void registerUser_ValidationError() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "") // Empty username triggers @NotEmpty
                        .param("password", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/auth/register"))
                .andExpect(model().hasErrors());

        verify(userService, times(0)).createUser(any(UserCreateDTO.class));
    }
}