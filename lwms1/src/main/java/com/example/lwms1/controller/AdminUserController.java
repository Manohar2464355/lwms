package com.example.lwms1.controller;

import com.example.lwms1.dto.UserCreateDTO;
import com.example.lwms1.dto.UserRoleUpdateDTO;
import com.example.lwms1.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/admin")
public class AdminUserController {

    private final UserService userService;

    private final DashboardService dashboardService;

    @Autowired
    public AdminUserController(UserService userService,

                               DashboardService dashboardService) {
        this.userService = userService;

        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAllAttributes(dashboardService.getAllStats());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.listAll());
        model.addAttribute("createForm", new UserCreateDTO());
        model.addAttribute("roleForm", new UserRoleUpdateDTO());
        return "admin/users/list";
    }

    @PostMapping("/users/create")
    public String create(@ModelAttribute("createForm") @Valid UserCreateDTO dto,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.listAll());
            model.addAttribute("roleForm", new UserRoleUpdateDTO());
            return "admin/users/list";
        }

        userService.createUser(dto);
        ra.addFlashAttribute("success", "User created successfully!");
        return "redirect:/admin/users";
    }



    @PostMapping("/users/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        userService.deleteUser(id);
        ra.addFlashAttribute("success", "User deleted.");
        return "redirect:/admin/users";
    }

}