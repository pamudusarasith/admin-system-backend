package lk.gov.mohe.adminsystem.role;

import jakarta.validation.Valid;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lk.gov.mohe.adminsystem.permission.Permission;
import lk.gov.mohe.adminsystem.permission.PermissionRepository;
import lk.gov.mohe.adminsystem.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lk.gov.mohe.adminsystem.role.RoleService;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ApiResponse<Void> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return roleService.createRole(request.name(), request.description(), request.permissions());
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateRole(
            @PathVariable Integer id,
            @Valid @RequestBody CreateRoleRequest request) {
        return roleService.updateRole(id, request.name(), request.description(), request.permissions());
    }

    @GetMapping
    public List<RoleDto> getAllRoles() {
        return roleService.getAllRoles();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Integer id) {
        return roleService.deleteRole(id);
    }
    

    public record CreateRoleRequest(
            String name,
            String description,
            List<String> permissions) {
    }
}
