package com.example.lwms1.controller;

import com.example.lwms1.dto.ShipmentDTO;
import com.example.lwms1.service.InventoryService;
import com.example.lwms1.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String list(Model model) {
        model.addAttribute("shipments", service.listAll());
        model.addAttribute("items", inventoryService.listAll());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ShipmentDTO());
        }
        return "admin/shipment/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        // Use the new service mapping method
        model.addAttribute("form", service.getDtoById(id));
        model.addAttribute("items", inventoryService.listAll());
        return "admin/shipment/edit";
    }

    @PostMapping("/update")
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
    public String create(@Valid @ModelAttribute("form") ShipmentDTO dto,
                         BindingResult result, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("shipments", service.listAll());
            model.addAttribute("items", inventoryService.listAll());
            return "admin/shipment/list";
        }
        service.create(dto);
        ra.addFlashAttribute("success", "New shipment registered!");
        return "redirect:/admin/shipments";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("success", "Shipment record removed and stock restored.");
        return "redirect:/admin/shipments";
    }

    @GetMapping("/track/{id}")
    public String track(@PathVariable Integer id, Model model) {
        model.addAttribute("shipment", service.get(id));
        return "admin/shipment/track";
    }
}