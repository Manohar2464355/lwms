package com.example.lwms1.config;

import com.example.lwms1.model.UserAccount;
import com.example.lwms1.repository.UserAccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final UserAccountRepository userRepo;

    public SecurityConfig(UserAccountRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserAccount u = userRepo.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            return User.withUsername(u.getUsername())
                    .password(u.getPassword())
                    .disabled(!u.isEnabled())
                    .authorities(u.getRoles().stream()
                            .map(r -> r.getName().startsWith("ROLE_") ? r.getName() : "ROLE_" + r.getName())
                            .toArray(String[]::new))
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
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(f -> f.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        // 1. Public
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/h2-console/**","/forgot-password", "/error/**").permitAll()
                        .requestMatchers("/admin/shipments/track/**").hasAnyRole("ADMIN", "USER")
                        // 2. Strict Admin Only (Users, Reports, Maintenance, Space)
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 3. Shared Resources (Inventory & Shipments)
                        // Note: Since InventoryController is @RequestMapping("/inventory"),
                        // we must allow it here for both roles.
                        .requestMatchers("/inventory/**").hasAnyRole("ADMIN", "USER")

                        // 4. Staff/User Dashboard
                        .requestMatchers("/user/**").hasAnyRole("ADMIN", "USER")

                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        // Change to "/" so your AuthController.rootRedirect handles the logic
                        .defaultSuccessUrl("/", true)
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