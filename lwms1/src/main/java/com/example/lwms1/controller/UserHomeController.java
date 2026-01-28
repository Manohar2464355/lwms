package com.example.lwms1.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasRole('USER')")
public class UserHomeController {


    @GetMapping("/home")
    public String userHome(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        return "user/home"; // Location: templates/user/home.html
    }
}