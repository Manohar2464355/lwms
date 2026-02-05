package com.example.lwms1.config;

import com.example.lwms1.model.Role;
import com.example.lwms1.model.UserAccount;
import com.example.lwms1.repository.UserAccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Optional;
import java.util.Set;

@Configuration
public class SecurityConfig {

    private final UserAccountRepository userRepo;

    public SecurityConfig(UserAccountRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                Optional<UserAccount> userOpt = userRepo.findByUsername(username);

                if (!userOpt.isPresent()) {
                    throw new UsernameNotFoundException("User not found: " + username);
                }

                UserAccount u = userOpt.get();
                Set<Role> roleSet = u.getRoles();
                String[] roles = new String[roleSet.size()];

                int i = 0;
                for (Role role : roleSet) {
                    String name = role.getName();
                    if (name.startsWith("ROLE_")) {
                        roles[i] = name.substring(5);
                    } else {
                        roles[i] = name;
                    }
                    i++;
                }
                return User.withUsername(u.getUsername())
                        .password(u.getPassword())
                        .roles(roles)
                        .build();
            }
        };
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/h2-console/**", "/error/**").permitAll()
                        .requestMatchers("/inventory/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/admin/shipments/track/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
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