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

  public List<MainCategoryDTO> getPermissionHierarchy() {
    List<PermissionCategory> allCategories = permissionCategoryRepository.findAll();
    List<Permission> allPermissions = permissionRepository.findAll();

    // Map: categoryId -> PermissionCategory
    Map<Integer, PermissionCategory> categoryMap =
        allCategories.stream().collect(Collectors.toMap(PermissionCategory::getId, c -> c));

    // Map: categoryId -> List<Permission>
    Map<Integer, List<Permission>> permissionsByCategory =
        allPermissions.stream()
            .filter(p -> p.getCategory() != null)
            .collect(Collectors.groupingBy(p -> p.getCategory().getId()));

    // Main categories: categories with no parent
    List<MainCategoryDTO> mainCategoryDTOs = new ArrayList<>();
    for (PermissionCategory mainCat :
        allCategories.stream().filter(c -> c.getParent() == null).toList()) {
      // Subcategories: categories whose parent is this main category
      List<SubCategoryDTO> subCategoryDTOs = new ArrayList<>();
      for (PermissionCategory subCat :
          allCategories.stream()
              .filter(c -> c.getParent() != null && c.getParent().getId().equals(mainCat.getId()))
              .toList()) {
        List<PermissionDTO> subCatPermissions =
            permissionsByCategory.getOrDefault(subCat.getId(), List.of()).stream()
                .map(
                    p ->
                        new PermissionDTO(
                            p.getId(), p.getName(), p.getLabel(), p.getDescription(), null))
                .collect(Collectors.toList());
        if (!subCatPermissions.isEmpty()) {
          subCategoryDTOs.add(
              new SubCategoryDTO(subCat.getId(), subCat.getName(), subCatPermissions));
        }
      }
      // Permissions directly under the main category (not in a subcategory)
      List<PermissionDTO> mainCatPermissions =
          permissionsByCategory.getOrDefault(mainCat.getId(), List.of()).stream()
              .map(
                  p ->
                      new PermissionDTO(
                          p.getId(), p.getName(), p.getLabel(), p.getDescription(), null))
              .collect(Collectors.toList());
      if (!mainCatPermissions.isEmpty()) {
        // Optionally, you can use a special subcategory name like "General" or "Other"
        subCategoryDTOs.add(0, new SubCategoryDTO(mainCat.getId(), "General", mainCatPermissions));
      }
      mainCategoryDTOs.add(
          new MainCategoryDTO(mainCat.getId(), mainCat.getName(), subCategoryDTOs));
    }
    return mainCategoryDTOs;
  }
}
