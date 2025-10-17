package lk.gov.mohe.adminsystem.role;

import java.util.*;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RoleController {
  private final RoleService roleService;

  @GetMapping("/roles")
  public ApiResponse<List<RoleDto>> getRoles(
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize) {
    Page<RoleDto> roles = roleService.getRoles(query, page, pageSize);
    return ApiResponse.paged(roles);
  }

  @PostMapping("/roles")
  public ApiResponse<Void> createRole(@RequestBody CreateOrUpdateRoleRequestDto request) {
    roleService.createRole(request);
    return ApiResponse.message("Role created successfully");
  }

  @PutMapping("/roles/{id}")
  public ApiResponse<Void> updateRole(
      @PathVariable Integer id, @RequestBody CreateOrUpdateRoleRequestDto request) {
    roleService.updateRole(id, request);
    return ApiResponse.message("Role updated successfully");
  }

  @DeleteMapping("/roles/{id}")
  public ApiResponse<Void> deleteRole(@PathVariable Integer id) {
    roleService.deleteRole(id);
    return ApiResponse.message("Role deleted successfully");
  }
}
