package com.example.lwms1.controller;

import com.example.lwms1.dto.UserCreateDTO;
import com.example.lwms1.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired; // Explicit import
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    // Using 'final' ensures this dependency cannot be changed once the app starts
    private final UserService userService;

    // Explicit Constructor Autowiring
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String rootRedirect(Authentication auth) {
        if (auth == null) return "redirect:/login";

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return isAdmin ? "redirect:/admin/dashboard" : "redirect:/user/home";
    }

    @GetMapping("/login")
    public String login() {
        return "admin/auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserCreateDTO());
        return "admin/auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userDto") UserCreateDTO userDto,
                               BindingResult result,
                               RedirectAttributes ra) {
        // Stop if standard validation (like @NotEmpty) fails
        if (result.hasErrors()) {
            return "admin/auth/register";
        }



        // The service layer handles saving the user
        userService.createUser(userDto);

        ra.addFlashAttribute("successMessage", "Registration successful! Please login.");
        return "redirect:/login";
    }
}