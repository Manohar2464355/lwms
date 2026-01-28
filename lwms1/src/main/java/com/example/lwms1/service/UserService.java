package com.example.lwms1.service;

import com.example.lwms1.dto.UserCreateDTO;
import com.example.lwms1.dto.UserRoleUpdateDTO;
import com.example.lwms1.exception.BusinessException;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.Role;
import com.example.lwms1.model.UserAccount;
import com.example.lwms1.repository.RoleRepository;
import com.example.lwms1.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UserService {

    // These must be declared here for the methods below to "see" them
    private final UserAccountRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public UserService(UserAccountRepository userRepo, RoleRepository roleRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    public List<UserAccount> listAll() {
        return userRepo.findAll();
    }

    public UserAccount createUser(UserCreateDTO dto) {
        if (userRepo.findByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessException("Username already exists: " + dto.getUsername());
        }
        UserAccount u = new UserAccount();
        u.setUsername(dto.getUsername());
        u.setEmail(dto.getEmail());
        u.setPassword(encoder.encode(dto.getPassword()));
        u.setEnabled(true);

        String roleName = "ROLE_" + (dto.getRole() == null ? "USER" : dto.getRole().toUpperCase());
        Role role = roleRepo.findByName(roleName)
                .orElseGet(() -> roleRepo.save(new Role(roleName)));
        u.setRoles(Set.of(role));

        return userRepo.save(u);
    }

    public UserAccount setUserRole(UserRoleUpdateDTO dto) {
        // userRepo is now recognized because it's a class field
        UserAccount user = userRepo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getUsername()));

        String roleName = "ROLE_" + dto.getRole().toUpperCase();
        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new BusinessException("Role not found: " + roleName));

        user.setRoles(Set.of(role));
        return userRepo.save(user);
    }

    public UserAccount grantRole(String username, String roleSimpleName) {
        UserAccount user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String roleName = "ROLE_" + roleSimpleName.toUpperCase();
        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new BusinessException("Role not found: " + roleName));

        user.getRoles().add(role);
        return userRepo.save(user);
    }

    public UserAccount revokeRole(String username, String roleSimpleName) {
        UserAccount user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String roleName = "ROLE_" + roleSimpleName.toUpperCase();
        user.getRoles().removeIf(r -> r.getName().equals(roleName));

        if (user.getRoles().isEmpty()) {
            throw new BusinessException("User must have at least one role");
        }
        return userRepo.save(user);
    }
}