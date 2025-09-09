package lk.gov.mohe.adminsystem.role;

import jakarta.validation.Valid;
import lk.gov.mohe.adminsystem.permission.Permission;
import lk.gov.mohe.adminsystem.permission.PermissionRepository;
import lk.gov.mohe.adminsystem.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor

public class RoleController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@Valid @RequestBody CreateRoleRequest request) {
        if (roleRepository.findByName(request.name()).isPresent()) {
            return ResponseEntity.badRequest().body("Role name already exists");
        }

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllByNameIsIn(request.permissions()));

        Role role = new Role();
        role.setName(request.name());
        role.setDescription(request.description());
        role.setPermissions(permissions);

        roleRepository.save(role);
        return ResponseEntity.ok("Role created successfully");
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<?> updateRole(
            @PathVariable Integer id,
            @Valid @RequestBody CreateRoleRequest request) {
        return roleRepository.findById(id)
                .map(role -> {
                    role.setName(request.name());
                    role.setDescription(request.description());
                    Set<Permission> permissions = new HashSet<>(
                            permissionRepository.findAllByNameIsIn(request.permissions()));
                    role.setPermissions(permissions);
                    roleRepository.save(role);
                    return ResponseEntity.ok("Role updated successfully");
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<Integer> roleIds = roles.stream().map(Role::getId).toList();
        List<Object[]> userCounts = userRepository.countUsersByRoleIds(roleIds);
        Map<Integer, Long> userCountMap = new HashMap<>();
        for (Object[] obj : userCounts) {
            Integer roleId = (Integer) obj[0];
            Long count = (Long) obj[1];
            userCountMap.put(roleId, count);
        }
        List<RoleDto> roleDtos = new ArrayList<>();
        for (Role role : roles) {
            RoleDto roleDto = new RoleDto(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getPermissions().stream().map(Permission::getName).toList(),
                userCountMap.getOrDefault(role.getId(), 0L)
            );
            roleDtos.add(roleDto);
        }
        return ResponseEntity.ok(roleDtos);
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Integer id) {
        return roleRepository.findById(id)
                .map(role -> {
                    roleRepository.delete(role);
                    return ResponseEntity.ok("Role deleted successfully");
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    

    public record CreateRoleRequest(
            String name,
            String description,
            List<String> permissions) {
    }
}
