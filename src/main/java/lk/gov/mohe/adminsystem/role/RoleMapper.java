package lk.gov.mohe.adminsystem.role;

import lk.gov.mohe.adminsystem.permission.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
  @Mapping(target = "userCount", ignore = true)
  RoleDto roleToRoleDto(Role role);

  default String permissionToName(Permission permission) {
    return permission.getName();
  }
}
