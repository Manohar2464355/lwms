package com.example.lwms1.controller;

import com.example.lwms1.dto.UserCreateDTO;
import com.example.lwms1.dto.UserRoleUpdateDTO;
import com.example.lwms1.model.Inventory;
import com.example.lwms1.service.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final InventoryService inventoryService;
    private final ShipmentService shipmentService; // <--- HIGHLIGHT: Add this

    public AdminUserController(UserService userService,
                               InventoryService inventoryService,
                               ShipmentService shipmentService) { // <--- HIGHLIGHT: Update constructor
        this.userService = userService;
        this.inventoryService = inventoryService;
        this.shipmentService = shipmentService;
    }

    /**
     * MERGED ADMIN DASHBOARD
     * URL: /admin/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 1. Inventory Data
        List<Inventory> inventoryList = inventoryService.listAll();
        model.addAttribute("inventoryCount", inventoryList.size());

        // 2. Real Shipment Data
        // HIGHLIGHT: Using shipmentService and matching HTML variable name
        model.addAttribute("activeShipmentsCount", shipmentService.listAll().size());

        // 3. User Data
        model.addAttribute("userCount", userService.listAll().size());

        // 4. Order Data - Placeholder (matches ${pendingOrdersCount} in HTML)
        model.addAttribute("pendingOrdersCount", 0);

        // 5. Space Occupancy Logic
        // HIGHLIGHT: Calculating a real percentage for ${warehouseUtilization}
        double totalCapacity = 500.0; // Assume your warehouse has 500 slots
        double utilization = (inventoryList.size() / totalCapacity) * 100;
        model.addAttribute("warehouseUtilization", String.format("%.1f%%", utilization));

        // 6. Maintenance Logic (Matches ${openMaintenanceTasks})
        long lowStockCount = inventoryList.stream()
                .filter(i -> i.getQuantity() != null && i.getQuantity() < 10)
                .count();
        model.addAttribute("openMaintenanceTasks", lowStockCount);

        // NOTE: Ensure your HTML file is in: src/main/resources/templates/admin/dashboard.html
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
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.listAll());
            model.addAttribute("roleForm", new UserRoleUpdateDTO());
            return "admin/users/list";
        }
        userService.createUser(dto);
        return "redirect:/admin/users?success";
    }

    @PostMapping("/users/set-role")
    public String setRole(@ModelAttribute("roleForm") @Valid UserRoleUpdateDTO dto,
                          BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.listAll());
            model.addAttribute("createForm", new UserCreateDTO());
            return "admin/users/list";
        }
        userService.setUserRole(dto);
        return "redirect:/admin/users?updated";
    }
}