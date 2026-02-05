package com.example.lwms1.controller;

import com.example.lwms1.dto.SpaceDTO;
import com.example.lwms1.service.SpaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/space")
public class SpaceController {

    private final SpaceService service;

    @Autowired
    public SpaceController(SpaceService service) {
        this.service = service;
    }

    @GetMapping
    public String usage(Model model) {
        model.addAttribute("spaces", service.listAll());
        model.addAttribute("form", new SpaceDTO());
        return "admin/space/usage";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("form") @Valid SpaceDTO dto,
                      BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("spaces", service.listAll());
            return "admin/space/usage";
        }
        service.create(dto);
        ra.addFlashAttribute("success", "Zone created successfully!");
        return "redirect:/admin/space";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        // Now using the service mapping method
        model.addAttribute("form", service.getDtoById(id));
        model.addAttribute("spaceId", id);
        return "admin/space/edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id,
                         @ModelAttribute("form") @Valid SpaceDTO dto,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("spaceId", id);
            return "admin/space/edit";
        }
        service.update(id, dto);
        ra.addFlashAttribute("success", "Zone updated successfully!");
        return "redirect:/admin/space";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("success", "Space deleted successfully.");
        return "redirect:/admin/space";
    }
}