package com.example.lwms1.config;

import com.example.lwms1.model.Role;
import com.example.lwms1.model.UserAccount;
import com.example.lwms1.repository.RoleRepository;
import com.example.lwms1.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepo,
                                   UserAccountRepository userRepo,
                                   PasswordEncoder encoder) {
        return args -> {
            // 1. Initialize Roles
            Role adminRole = roleRepo.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepo.save(new Role("ROLE_ADMIN")));

            roleRepo.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepo.save(new Role("ROLE_USER")));

            // 2. Initialize Admin User (The missing part!)
            if (userRepo.findByUsername("admin").isEmpty()) {
                UserAccount admin = new UserAccount();
                admin.setUsername("admin");
                admin.setEmail("admin@lwms.com");
                admin.setPassword(encoder.encode("admin123")); // Encrypts the password
                admin.setEnabled(true);
                admin.setRoles(Set.of(adminRole)); // Links user to the ADMIN role

                userRepo.save(admin);
                System.out.println(">> Security: Default Admin account created (admin/admin123)");
            }
        };
    }
}