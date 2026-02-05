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
        java.util.Optional<UserAccount> existing = userRepo.findByUsername(dto.getUsername());
        if (existing.isPresent()) {
            throw new BusinessException("Username already exists: " + dto.getUsername());
        }
        UserAccount u = new UserAccount();
        u.setUsername(dto.getUsername());
        u.setEmail(dto.getEmail());
        u.setPassword(encoder.encode(dto.getPassword()));
        u.setEnabled(true);

        String roleName = formatRole(dto.getRole());
        Optional<Role> roleOpt = roleRepo.findByName(roleName);
        Role role;

        if (roleOpt.isPresent()) {
            role = roleOpt.get();
        } else {
            Role newRole = new Role(roleName);
            role = roleRepo.save(newRole);
        }
        Set<Role> roles = new HashSet<Role>();
        roles.add(role);
        u.setRoles(roles);

        return userRepo.save(u);
    }

    public void deleteUser(Long id) {
        Optional<UserAccount> userOpt = userRepo.findById(id);
        if (userOpt.isPresent()) {
            UserAccount user = userOpt.get();
            if (user.getUsername().equalsIgnoreCase("admin")) {
                throw new BusinessException("The system administrator account cannot be deleted!");
            }
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