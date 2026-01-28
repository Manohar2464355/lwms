package com.example.lwms1.controller;

import com.example.lwms1.dto.ShipmentDTO;
import com.example.lwms1.model.Shipment;
import com.example.lwms1.service.InventoryService;
import com.example.lwms1.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/shipments")
public class ShipmentController {
    private final ShipmentService service;
    private final InventoryService inventoryService;

    public ShipmentController(ShipmentService service, InventoryService inventoryService) {
        this.service = service;
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("shipments", service.listAll());
        model.addAttribute("items", inventoryService.listAll());
        // Ensure "form" is present for the 'Record New Shipment' section in list.html
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ShipmentDTO());
        }
        return "shipment/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Shipment s = service.get(id);
        ShipmentDTO dto = new ShipmentDTO();
        dto.setShipmentId(s.getShipmentId());

        if (s.getInventory() != null) {
            dto.setItemId(s.getInventory().getItemId());
        }

        dto.setOrigin(s.getOrigin());
        dto.setDestination(s.getDestination());
        dto.setStatus(s.getStatus());
        dto.setExpectedDeliveryDate(s.getExpectedDeliveryDate());

        model.addAttribute("form", dto);
        model.addAttribute("items", inventoryService.listAll());
        return "shipment/edit";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("form") ShipmentDTO dto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("items", inventoryService.listAll());
            return "shipment/edit";
        }

        service.updateFullShipment(dto); // Make sure this method name exists in Service
        ra.addFlashAttribute("success", "Shipment updated successfully!");
        return "redirect:/shipments";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/receive")
    public String receive(@Valid @ModelAttribute("form") ShipmentDTO dto,
                          BindingResult result,
                          RedirectAttributes ra,
                          Model model) {
        if (result.hasErrors()) {
            // If validation fails (e.g., past date), reload the list with errors
            model.addAttribute("shipments", service.listAll());
            model.addAttribute("items", inventoryService.listAll());
            return "shipment/list";
        }

        service.receive(dto);
        ra.addFlashAttribute("success", "New shipment registered!");
        return "redirect:/shipments";
    }

    @GetMapping("/track/{id}")
    public String track(@PathVariable Integer id, Model model) {
        Shipment s = service.get(id);
        model.addAttribute("shipment", s);
        return "shipment/track";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("success", "Shipment record removed.");
        return "redirect:/shipments";
    }
}