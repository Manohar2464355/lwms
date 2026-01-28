package com.example.lwms1.dto;

import jakarta.validation.constraints.NotBlank;

public class UserRoleUpdateDTO {
    @NotBlank
    private String username;

    @NotBlank
    private String role; // e.g., "ADMIN"

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}