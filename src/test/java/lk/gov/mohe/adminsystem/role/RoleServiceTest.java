package lk.gov.mohe.adminsystem.role;

import lk.gov.mohe.adminsystem.permission.PermissionRepository;
import lk.gov.mohe.adminsystem.user.UserRepository;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleService roleService;

    public RoleServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateRole_NameExists() {
        when(roleRepository.findByName("Admin")).thenReturn(Optional.of(new Role()));
        ApiResponse<Void> response = roleService.createRole("Admin", "desc", List.of("READ"));
        assertTrue(response.errors() != null && !response.errors().isEmpty());
    }

    @Test
    void testCreateRole_Success() {
        when(roleRepository.findByName("User")).thenReturn(Optional.empty());
        when(permissionRepository.findAllByNameIsIn(anyList())).thenReturn(Collections.emptyList());
        ApiResponse<Void> response = roleService.createRole("User", "desc", List.of("READ"));
        assertFalse(response.errors() != null && !response.errors().isEmpty());
        assertEquals("Role created successfully", response.message());
    }

    @Test
    void testDeleteRole_NotFound() {
        when(roleRepository.findById(1)).thenReturn(Optional.empty());
        ApiResponse<Void> response = roleService.deleteRole(1);
        assertTrue(response.errors() != null && !response.errors().isEmpty());
    }
}