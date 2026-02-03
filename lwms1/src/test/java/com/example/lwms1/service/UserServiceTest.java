package com.example.lwms1.service;

import com.example.lwms1.dto.UserCreateDTO;
import com.example.lwms1.dto.UserRoleUpdateDTO;
import com.example.lwms1.exception.BusinessException;
import com.example.lwms1.exception.ResourceNotFoundException;
import com.example.lwms1.model.Role;
import com.example.lwms1.model.UserAccount;
import com.example.lwms1.repository.RoleRepository;
import com.example.lwms1.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock private UserAccountRepository userRepo;
    @Mock private RoleRepository roleRepo;
    @Mock private PasswordEncoder encoder;

    @InjectMocks
    private UserService userService;

    private UserAccount mockUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role("ROLE_USER");

        mockUser = new UserAccount();
        mockUser.setId(1L);
        mockUser.setUsername("johndoe");
        mockUser.setEmail("john@example.com");
        mockUser.setRoles(new HashSet<>(Set.of(userRole)));
    }

    @Test
    @DisplayName("Create: Should encode password and add ROLE_ prefix")
    void testCreateUser() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("newuser");
        dto.setPassword("rawPassword");
        dto.setRole("ADMIN"); // Should become ROLE_ADMIN

        when(userRepo.findByUsername("newuser")).thenReturn(Optional.empty());
        when(encoder.encode("rawPassword")).thenReturn("hashedPassword");
        when(roleRepo.findByName("ROLE_ADMIN")).thenReturn(Optional.of(new Role("ROLE_ADMIN")));
        when(userRepo.save(any(UserAccount.class))).thenAnswer(i -> i.getArgument(0));

        UserAccount result = userService.createUser(dto);

        assertEquals("hashedPassword", result.getPassword());
        assertTrue(result.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN")));
        verify(encoder).encode("rawPassword");
    }

    @Test
    @DisplayName("Create: Should throw exception if username exists")
    void testCreateUserDuplicate() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("johndoe");

        when(userRepo.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));

        assertThrows(BusinessException.class, () -> userService.createUser(dto));
    }

    @Test
    @DisplayName("Revoke: Should throw exception if attempting to remove last role")
    void testRevokeLastRole() {
        when(userRepo.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));

        // Attempting to remove ROLE_USER (the only role he has)
        assertThrows(BusinessException.class, () -> userService.revokeRole("johndoe", "USER"));
    }

    @Test
    @DisplayName("Grant: Should add new role to existing set")
    void testGrantRole() {
        Role adminRole = new Role("ROLE_ADMIN");
        when(userRepo.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));
        when(roleRepo.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepo.save(any(UserAccount.class))).thenReturn(mockUser);

        UserAccount result = userService.grantRole("johndoe", "ADMIN");

        assertEquals(2, result.getRoles().size());
        verify(userRepo).save(mockUser);
    }

    @Test
    @DisplayName("Formatting: Helper method should handle various role inputs")
    void testRoleFormatting() {
        // Since formatRole is private, we test it through public methods or reflection.
        // Here we test through setUserRole to verify the formatting logic.
        UserRoleUpdateDTO dto = new UserRoleUpdateDTO();
        dto.setUsername("johndoe");
        dto.setRole("manager"); // Should become ROLE_MANAGER

        when(userRepo.findByUsername("johndoe")).thenReturn(Optional.of(mockUser));
        when(roleRepo.findByName("ROLE_MANAGER")).thenReturn(Optional.of(new Role("ROLE_MANAGER")));

        userService.setUserRole(dto);

        verify(roleRepo).findByName("ROLE_MANAGER");
    }
}