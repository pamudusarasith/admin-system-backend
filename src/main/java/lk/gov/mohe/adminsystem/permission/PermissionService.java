package lk.gov.mohe.adminsystem.permission;

import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionService {
  private final PermissionRepository permissionRepository;
  private final PermissionCategoryRepository permissionCategoryRepository;
  private final PermissionMapper permissionMapper;

  public List<PermissionCategoryDto> getPermissions() {
    List<PermissionCategory> categories = permissionCategoryRepository.findAll();
    Map<Integer, PermissionCategoryDto> categoryDtoMap =
        categories.stream()
            .collect(Collectors.toMap(PermissionCategory::getId, permissionMapper::categoryToDto));
    List<Permission> permissions = permissionRepository.findAll();
    permissions.forEach(
        p -> {
          PermissionDto permissionDto = permissionMapper.permissionToDto(p);
          PermissionCategoryDto categoryDto = categoryDtoMap.get(p.getCategory().getId());
          if (categoryDto.getPermissions() == null) {
            categoryDto.setPermissions(new ArrayList<>());
          }
          categoryDto.getPermissions().add(permissionDto);
        });
    categories.forEach(
        c -> {
          if (c.getParent() != null) {
            PermissionCategoryDto categoryDto = categoryDtoMap.remove(c.getId());
            PermissionCategoryDto parentDto = categoryDtoMap.get(c.getParent().getId());
            if (parentDto.getSubCategories() == null) {
              parentDto.setSubCategories(new ArrayList<>());
            }
            parentDto.getSubCategories().add(categoryDto);
          }
        });
    return new ArrayList<>(categoryDtoMap.values());
  }
}
