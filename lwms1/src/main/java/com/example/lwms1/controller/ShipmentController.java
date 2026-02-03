package com.example.lwms1.controller;

import com.example.lwms1.dto.ShipmentDTO;
import com.example.lwms1.model.Inventory;
import com.example.lwms1.model.Shipment;
import com.example.lwms1.service.InventoryService;
import com.example.lwms1.service.ShipmentService;
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
@RequestMapping("/admin/shipments")
public class ShipmentController {

    private final ShipmentService service;
    private final InventoryService inventoryService;

    @Autowired
    public ShipmentController(ShipmentService service, InventoryService inventoryService) {
        this.service = service;
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String list(Model model) {
        model.addAttribute("shipments", service.listAll());
        model.addAttribute("items", inventoryService.listAll());

        // Ensure the form object exists for Thymeleaf
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ShipmentDTO());
        }
        return "admin/shipment/list";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Shipment s = service.get(id);

        // Simple manual mapping from Entity to DTO
        ShipmentDTO dto = new ShipmentDTO();
        dto.setShipmentId(s.getShipmentId());
        dto.setOrigin(s.getOrigin());
        dto.setDestination(s.getDestination());
        dto.setStatus(s.getStatus());
        dto.setExpectedDeliveryDate(s.getExpectedDeliveryDate());

        // Handle the related item ID carefully
        if (s.getInventory() != null) {
            dto.setItemId(s.getInventory().getItemId());
        }

        model.addAttribute("form", dto);
        model.addAttribute("items", inventoryService.listAll());
        return "admin/shipment/edit";
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@Valid @ModelAttribute("form") ShipmentDTO dto,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("items", inventoryService.listAll());
            return "admin/shipment/edit";
        }

        service.updateFullShipment(dto);
        ra.addFlashAttribute("success", "Shipment updated successfully!");
        return "redirect:/admin/shipments";
    }

    @PostMapping("/receive")
    @PreAuthorize("hasRole('ADMIN')")
    public String receive(@Valid @ModelAttribute("form") ShipmentDTO dto,
                          BindingResult result, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("shipments", service.listAll());
            model.addAttribute("items", inventoryService.listAll());
            return "admin/shipment/list";
        }

        service.receive(dto);
        ra.addFlashAttribute("success", "New shipment registered!");
        return "redirect:/admin/shipments";
    }

    @GetMapping("/track/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String track(@PathVariable Integer id, Model model) {
        model.addAttribute("shipment", service.get(id));
        return "admin/shipment/track";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("success", "Shipment record removed.");
        return "redirect:/admin/shipments";
    }
}