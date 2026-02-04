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

    /**
     * FIXED: Added this method to satisfy the AdminUserController call.
     * This replaces a user's roles with the single one selected in the UI.
     */
    public UserAccount setUserRole(UserRoleUpdateDTO dto) {
        Optional<UserAccount> userOpt = userRepo.findByUsername(dto.getUsername());
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found: " + dto.getUsername());
        }
        UserAccount user = userOpt.get();

        String roleName = formatRole(dto.getRole());
        Optional<Role> roleOpt = roleRepo.findByName(roleName);

        Role role;
        if (roleOpt.isEmpty()) {
            // Self-healing: Create the role if it doesn't exist in the DB yet
            role = roleRepo.save(new Role(roleName));
        } else {
            role = roleOpt.get();
        }

        // Using a HashSet to ensure a fresh, clean role assignment
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        return userRepo.save(user);
    }
    public UserAccount createUser(UserCreateDTO dto) {
        // 1. Check if user already exists
        Optional<UserAccount> existing = userRepo.findByUsername(dto.getUsername());
        if (existing.isPresent()) {
            throw new BusinessException("Username already exists: " + dto.getUsername());
        }

        UserAccount u = new UserAccount();
        u.setUsername(dto.getUsername());
        u.setEmail(dto.getEmail());
        u.setPassword(encoder.encode(dto.getPassword()));
        u.setEnabled(true);

        // 2. Format the role name (e.g., "ADMIN" -> "ROLE_ADMIN")
        String roleName = formatRole(dto.getRole());

        // 3. THE FIX: Try to find the existing role FIRST.
        // If it exists, use it. Only if it's missing, save a new one.
        Role role = roleRepo.findByName(roleName)
                .orElseGet(() -> roleRepo.save(new Role(roleName)));

        // 4. Assign and save
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        u.setRoles(roles);

        return userRepo.save(u);
    }

    public UserAccount grantRole(String username, String roleSimpleName) {
        Optional<UserAccount> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found: " + username);
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
            throw new ResourceNotFoundException("User not found: " + username);
        }
        UserAccount user = userOpt.get();

        String roleName = formatRole(roleSimpleName);

        Role toRemove = null;
        for (Role r : user.getRoles()) {
            if (r.getName().equals(roleName)) {
                toRemove = r;
                break;
            }
        }

        if (toRemove != null) {
            user.getRoles().remove(toRemove);
        }

        if (user.getRoles().isEmpty()) {
            throw new BusinessException("User must have at least one role!");
        }

        return userRepo.save(user);
    }

    public void deleteUser(Long id) {
        // 1. Find the user first to check their username
        Optional<UserAccount> userOpt = userRepo.findById(id);

        // 2. Simple check if user exists
        if (userOpt.isPresent()) {
            UserAccount user = userOpt.get();

            // 3. Simple IF check to protect the 'admin' account
            if (user.getUsername().equalsIgnoreCase("admin")) {
                throw new BusinessException("The system administrator account cannot be deleted!");
            }

            // 4. Delete if it's not the admin
            userRepo.deleteById(id);
        } else {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
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