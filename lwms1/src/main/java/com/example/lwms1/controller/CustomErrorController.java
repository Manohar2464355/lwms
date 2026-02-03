package com.example.lwms1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorController {

    @GetMapping("/error/403")
    public String accessDenied() {
        // This points to src/main/resources/templates/error/403.html
        return "admin/error/403";
    }
}