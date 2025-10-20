package lk.gov.mohe.adminsystem.role;

import lk.gov.mohe.adminsystem.permission.Permission;
import lk.gov.mohe.adminsystem.permission.PermissionRepository;
import lk.gov.mohe.adminsystem.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleService roleService;

    private Role role;
    private CreateOrUpdateRoleRequestDto createDto;
    private CreateOrUpdateRoleRequestDto updateDto;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("Admin");

        createDto = new CreateOrUpdateRoleRequestDto("New Role", "Description", List.of("user:read"));
        updateDto = new CreateOrUpdateRoleRequestDto("Updated Role", "Updated Description", List.of("user:write"));
    }

    @Test
    void getRoles_ShouldMapAndSetUserCounts() {
        // Given
        // Corrected: Create a mock RoleDto that matches the new class structure
        RoleDto roleDto = new RoleDto();
        roleDto.setId(1);
        roleDto.setName("Admin");
        roleDto.setDescription("Admin role");
        roleDto.setPermissions(Collections.singletonList("user:read"));
        // userCount will be set by the service

        Page<Role> pagedRoles = new PageImpl<>(Collections.singletonList(role));
        when(roleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pagedRoles);
        when(roleMapper.roleToRoleDto(any(Role.class))).thenReturn(roleDto);

        Object[] userCountResult = {1, 5L}; // roleId 1 has 5 users
        when(userRepository.countUsersByRoleIds(List.of(1))).thenReturn(Collections.singletonList(userCountResult));

        // When
        Page<RoleDto> result = roleService.getRoles("admin", 0, 10);

        // Then
        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        // Verify the user count was correctly set on the DTO
        assertEquals(5L, result.getContent().get(0).getUserCount());
    }


    @Test
    void createRole_ShouldSucceed_WhenNameIsUniqueAndPermissionsAreValid() {
        Permission validPermission = new Permission();
        validPermission.setName("user:read");
        when(roleRepository.existsByNameIgnoreCase(createDto.name())).thenReturn(false);
        when(permissionRepository.findAllByNameIsIn(createDto.permissions())).thenReturn(Collections.singletonList(validPermission));

        roleService.createRole(createDto);

        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void createRole_ShouldThrowConflict_WhenNameExists() {
        when(roleRepository.existsByNameIgnoreCase(createDto.name())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> roleService.createRole(createDto));
    }

    @Test
    void updateRole_ShouldSucceed_WhenDataIsValid() {
        Permission validPermission = new Permission();
        validPermission.setName("user:write");
        when(roleRepository.findById(1)).thenReturn(Optional.of(role));
        when(roleRepository.existsByNameIgnoreCase(updateDto.name())).thenReturn(false);
        when(permissionRepository.findAllByNameIsIn(updateDto.permissions())).thenReturn(Collections.singletonList(validPermission));

        roleService.updateRole(1, updateDto);

        verify(roleRepository, times(1)).save(role);
        assertEquals("Updated Role", role.getName());
    }

    @Test
    void deleteRole_ShouldSucceed_WhenRoleIsNotInUse() {
        when(roleRepository.findById(1)).thenReturn(Optional.of(role));
        when(userRepository.countByRoleId(1)).thenReturn(0);

        roleService.deleteRole(1);

        verify(roleRepository, times(1)).delete(role);
    }

    @Test
    void deleteRole_ShouldThrowBadRequest_WhenRoleIsInUse() {
        when(roleRepository.findById(1)).thenReturn(Optional.of(role));
        when(userRepository.countByRoleId(1)).thenReturn(5);

        assertThrows(ResponseStatusException.class, () -> roleService.deleteRole(1));
    }
}