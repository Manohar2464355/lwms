
package com.example.lwms1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public class UserCreateDTO {
    @NotBlank @Size(min = 3, max = 60)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email structure")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)\\.(.+)$", message = "Email must contain a valid domain (e.g., .com, .org)")
    private String email;

    @NotBlank @Size(min = 6, max = 72)
    private String password;

    private String role;

    public UserCreateDTO() {}

    // --- Add Getter and Setter for Email ---
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}