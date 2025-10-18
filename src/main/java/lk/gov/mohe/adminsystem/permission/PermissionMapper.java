package lk.gov.mohe.adminsystem.permission;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
  PermissionDto permissionToDto(Permission permission);

  @Mapping(target = "permissions", ignore = true)
  @Mapping(target = "subCategories", ignore = true)
  PermissionCategoryDto categoryToDto(PermissionCategory category);
}
