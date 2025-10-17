package lk.gov.mohe.adminsystem.role;

import java.util.List;

public record RoleDto(
    Integer id, String name, String description, List<PermissionInfo> permissions, long userCount) {
  public record PermissionInfo(
      String mainCategory, String subCategory,String name, String label, String description) {}
}
