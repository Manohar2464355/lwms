package com.example.lwms1.service;

import com.example.lwms1.dto.UserCreateDTO;
import com.example.lwms1.dto.UserRoleUpdateDTO;
import com.example.lwms1.exception.BusinessException;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.Role;
import com.example.lwms1.model.UserAccount;
import com.example.lwms1.repository.RoleRepository;
import com.example.lwms1.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserService {

    private final UserAccountRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    @Autowired
    public UserService(UserAccountRepository userRepo, RoleRepository roleRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    public List<UserAccount> listAll() {
        return userRepo.findAll();
    }

    public UserAccount createUser(UserCreateDTO dto) {
        // 1. Check if user already exists
        Optional<UserAccount> existing = userRepo.findByUsername(dto.getUsername());
        if (existing.isPresent()) {
            throw new BusinessException("Username already exists: " + dto.getUsername());
        }

        // 2. Map DTO to Entity
        UserAccount u = new UserAccount();
        u.setUsername(dto.getUsername());
        u.setEmail(dto.getEmail());
        u.setPassword(encoder.encode(dto.getPassword())); // Encrypting the password
        u.setEnabled(true);

        // 3. Handle Role
        String roleName = formatRole(dto.getRole());
        Optional<Role> roleOpt = roleRepo.findByName(roleName);

        Role role;
        if (roleOpt.isPresent()) {
            role = roleOpt.get();
        } else {
            role = roleRepo.save(new Role(roleName));
        }

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        u.setRoles(roles);

        return userRepo.save(u);
    }

    public UserAccount grantRole(String username, String roleSimpleName) {
        Optional<UserAccount> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }
        UserAccount user = userOpt.get();

        String roleName = formatRole(roleSimpleName);
        Optional<Role> roleOpt = roleRepo.findByName(roleName);
        if (roleOpt.isEmpty()) {
            throw new BusinessException("Role not found: " + roleName);
        }

        user.getRoles().add(roleOpt.get());
        return userRepo.save(user);
    }

    public UserAccount revokeRole(String username, String roleSimpleName) {
        Optional<UserAccount> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }
        UserAccount user = userOpt.get();

        String roleName = formatRole(roleSimpleName);

        // SIMPLE FOR-LOOP REPLACEMENT FOR removeIf
        Role roleToRemove = null;
        for (Role r : user.getRoles()) {
            if (r.getName().equals(roleName)) {
                roleToRemove = r;
                break;
            }
        }

        if (roleToRemove != null) {
            user.getRoles().remove(roleToRemove);
        }

        if (user.getRoles().isEmpty()) {
            throw new BusinessException("User must have at least one role!");
        }

        return userRepo.save(user);
    }

    private String formatRole(String role) {
        if (role == null || role.isEmpty()) {
            return "ROLE_USER";
        }
        String upperRole = role.toUpperCase();
        if (upperRole.startsWith("ROLE_")) {
            return upperRole;
        }
        return "ROLE_" + upperRole;
    }
}