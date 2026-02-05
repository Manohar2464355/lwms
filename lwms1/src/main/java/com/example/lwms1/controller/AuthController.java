package com.example.lwms1.controller;

import com.example.lwms1.dto.UserCreateDTO;
import com.example.lwms1.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collection;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String rootRedirect(Authentication auth) {
        if (auth == null) {
            return "redirect:/login";
        }
        boolean isAdmin = false;
        @SuppressWarnings("unchecked")
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) auth.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                isAdmin = true;
                break;
            }
        }
        if (isAdmin) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/user/home";
        }
    }

    @GetMapping("/login")
    public String login() {
        return "admin/auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model,RedirectAttributes ra) {
        model.addAttribute("userDto", new UserCreateDTO());
        ra.addFlashAttribute("infoMessage", " fill all the fields correctly!");
        return "admin/auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userDto") UserCreateDTO userDto,
                               BindingResult result,
                               RedirectAttributes ra) {

        if (result.hasErrors()) {
            return "admin/auth/register";
        }


        userService.createUser(userDto);

        ra.addFlashAttribute("successMessage", "Registration successful! Please login.");
        return "redirect:/login";
    }
    }
