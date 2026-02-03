package com.example.lwms1.config;

import com.example.lwms1.model.UserAccount;
import com.example.lwms1.repository.UserAccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    private final UserAccountRepository userRepo;

    public SecurityConfig(UserAccountRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserAccount u = userRepo.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username));

            // Get role names without the "ROLE_" prefix because .roles() adds it for us
            String[] roles = u.getRoles().stream()
                    .map(role -> role.getName().replace("ROLE_", ""))
                    .toArray(String[]::new);

            return User.withUsername(u.getUsername())
                    .password(u.getPassword())
                    .roles(roles) // Simplest way to assign roles
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Removed DaoAuthenticationProvider bean - Spring Boot automates this!

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Simplified for testing; use Customizer.withDefaults() for production
                .authorizeHttpRequests(auth -> auth
                        // 1. PUBLIC ASSETS & AUTH
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/h2-console/**", "/error/**").permitAll()

                        // 2. SHARED ACCESS (Inventory & Shipment Tracking)
                        // Inventory handles both /inventory and /admin/shipments/track/**
                        .requestMatchers("/inventory/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/admin/shipments/track/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/user/**").hasAnyRole("ADMIN", "USER")

                        // 3. ADMIN-ONLY MODULES
                        // Covers /admin/users, /admin/maintenance, /admin/reports, /admin/space, /admin/shipments
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 4. CATCH-ALL
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true) // Redirects to root where AuthController decides the destination
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"));

        return http.build();
    }
}