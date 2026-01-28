
package com.example.lwms1.controller;

import com.example.lwms1.dto.SpaceAllocationDTO;
import com.example.lwms1.dto.SpaceDTO;
import com.example.lwms1.service.SpaceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * ADMIN-only Space management under /admin/space.
 */
@Controller
@RequestMapping("/admin/space")
@PreAuthorize("hasRole('ADMIN')")
public class SpaceController {

    private final SpaceService service;

    public SpaceController(SpaceService service) {
        this.service = service;
    }

    @GetMapping("")
    public String usage(Model model) {
        model.addAttribute("spaces", service.listAll());
        model.addAttribute("form", new SpaceDTO());
        model.addAttribute("alloc", new SpaceAllocationDTO());
        return "space/usage";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("form") @Valid SpaceDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("spaces", service.listAll());
            model.addAttribute("alloc", new SpaceAllocationDTO());
            return "space/usage";
        }

        // Instead of just passing the DTO, the service will now handle the initialization
        service.create(dto);

        return "redirect:/admin/space";
    }
    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id,
                         @ModelAttribute("form") @Valid SpaceDTO dto,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("spaces", service.listAll());
            model.addAttribute("alloc", new SpaceAllocationDTO());
            return "space/usage";
        }
        service.update(id, dto);
        return "redirect:/admin/space";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/admin/space";
    }

    @PostMapping("/allocate/{id}")
    public String allocate(@PathVariable Integer id,
                           @ModelAttribute("alloc") @Valid SpaceAllocationDTO dto) {
        service.allocate(id, dto);
        return "redirect:/admin/space";
    }


    @PostMapping("/free/{id}")
    public String free(@PathVariable Integer id,
                       @ModelAttribute("alloc") @Valid SpaceAllocationDTO dto) {
        service.free(id, dto);
        return "redirect:/admin/space";
    }
}
