package lk.gov.mohe.adminsystem.role;

import java.util.*;
import java.util.stream.Collectors;
import lk.gov.mohe.adminsystem.permission.Permission;
import lk.gov.mohe.adminsystem.permission.PermissionRepository;
import lk.gov.mohe.adminsystem.user.UserRepository;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final UserRepository userRepository;

  // Get all roles
  public List<RoleDto> getAllRoles() {
    List<Role> roles = roleRepository.findAll();
    List<Integer> roleIds = roles.stream().map(Role::getId).toList();

    // Count users per role
    List<Object[]> userCounts = userRepository.countUsersByRoleIds(roleIds);
    Map<Integer, Long> userCountMap = new HashMap<>();
    for (Object[] obj : userCounts) {
      userCountMap.put((Integer) obj[0], (Long) obj[1]);
    }

    return roles.stream()
        .map(
            role ->
                new RoleDto(
                    role.getId(),
                    role.getName(),
                    role.getDescription(),
                    role.getPermissions().stream()
                        .map(Permission::getName)
                        .collect(Collectors.toList()),
                    userCountMap.getOrDefault(role.getId(), 0L)))
        .collect(Collectors.toList());
  }

  // Create a role
  public ApiResponse<Void> createRole(
      String name, String description, List<String> permissionNames) {
    if (roleRepository.findByName(name).isPresent()) {
      return ApiResponse.error("Role name already exists", null);
    }

    Set<Permission> permissions =
        new HashSet<>(permissionRepository.findAllByNameIsIn(permissionNames));
    Role role = new Role();
    role.setName(name);
    role.setDescription(description);
    role.setPermissions(permissions);

    roleRepository.save(role);
    return ApiResponse.message("Role created successfully");
  }

  // Update a role
  public ApiResponse<Void> updateRole(
      Integer id, String name, String description, List<String> permissionNames) {
    return roleRepository
        .findById(id)
        .map(
            role -> {
              role.setName(name);
              role.setDescription(description);
              role.setPermissions(
                  new HashSet<>(permissionRepository.findAllByNameIsIn(permissionNames)));
              roleRepository.save(role);
              return ApiResponse.message("Role updated successfully");
            })
        .orElseGet(() -> ApiResponse.error("Role not found", null));
  }

  // Delete a role
  public ApiResponse<Void> deleteRole(Integer id) {
    return roleRepository
        .findById(id)
        .map(
            role -> {
              roleRepository.delete(role);
              return ApiResponse.message("Role deleted successfully");
            })
        .orElseGet(() -> ApiResponse.error("Role not found", null));
  }
}
