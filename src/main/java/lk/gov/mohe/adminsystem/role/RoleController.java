package lk.gov.mohe.adminsystem.role;

import jakarta.validation.Valid;
import java.util.*;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
      @PathVariable Integer id, @Valid @RequestBody CreateRoleRequest request) {
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

  public record CreateRoleRequest(String name, String description, List<String> permissions) {}
}
