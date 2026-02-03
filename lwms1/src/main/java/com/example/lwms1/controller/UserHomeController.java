package com.example.lwms1.controller;

import com.example.lwms1.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserHomeController {

    private final InventoryService inventoryService;
    private final ShipmentService shipmentService;
    private final SpaceService spaceService;
    private final MaintenanceService maintenanceService;
    private final ReportService reportService;
    private final DashboardService dashboardService;

    @Autowired
    public UserHomeController(InventoryService inventoryService,
                              ShipmentService shipmentService,
                              SpaceService spaceService,
                              MaintenanceService maintenanceService,
                              ReportService reportService,
                              DashboardService dashboardService) {
        this.inventoryService = inventoryService;
        this.shipmentService = shipmentService;
        this.spaceService = spaceService;
        this.maintenanceService = maintenanceService;
        this.reportService = reportService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/home")
    public String userHome(Authentication auth, Model model) {
        // Simple string retrieval for the greeting
        model.addAttribute("username", auth.getName());

        // Push all stats (counts) to the dashboard cards
        model.addAllAttributes(dashboardService.getAllStats());
        return "user/home";
    }

    @GetMapping("/inventory")
    public String viewInventory(Model model) {
        model.addAttribute("items", inventoryService.listAll());
        return "user/inventory";
    }

    @GetMapping("/shipments")
    public String viewShipments(Model model) {
        model.addAttribute("shipmentList", shipmentService.listAll());
        return "user/shipment";
    }

    @GetMapping("/space")
    public String viewSpace(Model model) {
        model.addAttribute("spaces", spaceService.listAll());
        return "user/space";
    }

    @GetMapping("/maintenance")
    public String viewMaintenance(Model model) {
        model.addAttribute("schedules", maintenanceService.listAll());
        return "user/maintenance";
    }

    @GetMapping("/reports")
    public String viewUserReport(Model model) {
        model.addAttribute("reports", reportService.listAll());
        return "user/reports";
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("roles", auth.getAuthorities());
        return "user/profile";
    }

    @GetMapping("/shipments/track/{id}")
    public String trackShipment(@PathVariable Integer id, Model model) {
        model.addAttribute("shipment", shipmentService.get(id));
        return "user/track";
    }
}