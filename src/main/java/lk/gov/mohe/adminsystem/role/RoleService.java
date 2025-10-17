package lk.gov.mohe.adminsystem.role;

import java.util.*;
import lk.gov.mohe.adminsystem.permission.Permission;
import lk.gov.mohe.adminsystem.permission.PermissionRepository;
import lk.gov.mohe.adminsystem.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RoleService {
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final RoleMapper roleMapper;
  private final PermissionRepository permissionRepository;

  @Transactional(readOnly = true)
  public Page<RoleDto> getRoles(Integer page, Integer pageSize) {
    Pageable pageable = PageRequest.of(page, pageSize);
    Page<Role> roles = roleRepository.findAll(pageable);
    Page<RoleDto> roleDtos = roles.map(roleMapper::roleToRoleDto);

    List<Integer> roleIds = roleDtos.stream().map(RoleDto::getId).toList();
    List<Object[]> userCounts = userRepository.countUsersByRoleIds(roleIds);
    Map<Integer, Integer> roleIdToCountMap = new HashMap<>();
    for (Object[] obj : userCounts) {
      Integer roleId = (Integer) obj[0];
      Long count = (Long) obj[1];
      roleIdToCountMap.put(roleId, count.intValue());
    }

    roleDtos.forEach(
        dto -> {
          dto.setUserCount(roleIdToCountMap.getOrDefault(dto.getId(), 0));
        });

    return roleDtos;
  }

  @Transactional
  public void createRole(CreateOrUpdateRoleRequestDto request) {
    if (roleRepository.existsByNameIgnoreCase(request.name())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Role name already exists");
    }

    List<Permission> permissions = permissionRepository.findAllByNameIsIn(request.permissions());
    if (permissions.size() != request.permissions().size()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "One or more permissions are invalid");
    }

    Role role = new Role();
    role.setName(request.name());
    role.setDescription(request.description());
    role.setPermissions(new HashSet<>(permissions));
    roleRepository.save(role);
  }

  @Transactional
  public void updateRole(Integer id, CreateOrUpdateRoleRequestDto request) {
    Role role =
        roleRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

    if (!role.getName().equalsIgnoreCase(request.name())
        && roleRepository.existsByNameIgnoreCase(request.name())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Role name already exists");
    }

    List<Permission> permissions = permissionRepository.findAllByNameIsIn(request.permissions());
    if (permissions.size() != request.permissions().size()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "One or more permissions are invalid");
    }

    role.setName(request.name());
    role.setDescription(request.description());
    role.setPermissions(new HashSet<>(permissions));
    roleRepository.save(role);
  }

  @Transactional
  public void deleteRole(Integer id) {
    Role role =
        roleRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

    int userCount = userRepository.countByRoleId(role.getId());
    if (userCount > 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot delete role assigned to users");
    }

    roleRepository.delete(role);
  }
}
