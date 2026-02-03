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
    CommandLineRunner initDatabase(RoleRepository roleRepo, UserAccountRepository userRepo, PasswordEncoder encoder) {
        return args -> {
            // Create Role if it doesn't exist
            Role adminRole = roleRepo.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepo.save(new Role("ROLE_ADMIN")));

            // Create Admin User if they don't exist
            if (userRepo.findByUsername("admin").isEmpty()) {
                UserAccount admin = new UserAccount();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123")); // Encryption is mandatory
                admin.setRoles(Set.of(adminRole));
                admin.setEnabled(true);

                userRepo.save(admin);
            }
        };
    }
}