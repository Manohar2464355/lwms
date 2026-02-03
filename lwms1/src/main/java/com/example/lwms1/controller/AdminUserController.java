package com.example.lwms1.controller;

import com.example.lwms1.dto.UserCreateDTO;
import com.example.lwms1.dto.UserRoleUpdateDTO;
import com.example.lwms1.model.Space;
import com.example.lwms1.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final InventoryService inventoryService;
    private final ShipmentService shipmentService;
    private final MaintenanceService maintenanceService;
    private final ReportService reportService;
    private final SpaceService spaceService;
    // Inside AdminUserController
    private final DashboardService dashboardService; // Add this

    @Autowired
    public AdminUserController(UserService userService,
                               InventoryService inventoryService,
                               ShipmentService shipmentService,
                               MaintenanceService maintenanceService,
                               ReportService reportService,
                               SpaceService spaceService,
                               DashboardService dashboardService) { // Add to constructor
        this.userService = userService;
        this.inventoryService = inventoryService;
        this.shipmentService = shipmentService;
        this.maintenanceService = maintenanceService;
        this.reportService = reportService;
        this.spaceService = spaceService;
        this.dashboardService = dashboardService; // Assign it
    }

    /**
     * DASHBOARD LOGIC
     * Note: In a professional app, move this calculation to a DashboardService.
     */
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAllAttributes(dashboardService.getAllStats());
        return "admin/dashboard";
    }
    /**
     * USER MANAGEMENT
     */
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

        // No try-catch needed! GlobalExceptionHandler handles Business/Data errors.
        userService.createUser(dto);
        ra.addFlashAttribute("success", "User created successfully!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/set-role")
    public String setRole(@ModelAttribute("roleForm") @Valid UserRoleUpdateDTO dto,
                          BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.listAll());
            model.addAttribute("createForm", new UserCreateDTO());
            return "admin/users/list";
        }

        userService.setUserRole(dto);
        ra.addFlashAttribute("success", "User role updated!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        // If the user has active shipments/tasks,
        // DataIntegrityViolationException will trigger in GlobalHandler.
        userService.deleteUser(id);
        ra.addFlashAttribute("success", "User deleted.");
        return "redirect:/admin/users";
    }

}