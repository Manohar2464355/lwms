package com.example.lwms1.controller;

import com.example.lwms1.dto.InventoryDTO;
import com.example.lwms1.service.InventoryService;
import com.example.lwms1.service.SpaceService; // Added
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final SpaceService spaceService; // Added

    public InventoryController(InventoryService inventoryService, SpaceService spaceService) {
        this.inventoryService = inventoryService;
        this.spaceService = spaceService;
    }

    @GetMapping
    public String listInventory(Model model) {
        model.addAttribute("items", inventoryService.listAll());
        model.addAttribute("inventoryDTO", new InventoryDTO());
        model.addAttribute("spaces", spaceService.listAll()); // Critical: Sends zones to dropdown
        return "inventory/list";
    }

    @PostMapping("/add")
    public String addItem(@Valid @ModelAttribute("inventoryDTO") InventoryDTO dto, RedirectAttributes ra) {
        try {
            inventoryService.create(dto);
            return "redirect:/inventory?success";
        } catch (Exception e) {
            // This catches the "Not enough room" error from the service
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/inventory?error";
        }
    }

    @PostMapping("/update/{id}")
    public String updateItem(@PathVariable Integer id, @Valid @ModelAttribute("inventoryDTO") InventoryDTO dto) {
        inventoryService.update(id, dto);
        return "redirect:/inventory?updated";
    }

    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable Integer id) {
        inventoryService.delete(id);
        return "redirect:/inventory?deleted";
    }
}