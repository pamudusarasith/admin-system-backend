package lk.gov.mohe.adminsystem.permission;

public record PermissionDTO(
    Integer id, String name, String label, String description, PermissionCategoryDTO category) {
  public static PermissionDTO fromEntity(Permission permission) {
    return new PermissionDTO(
        permission.getId(),
        permission.getName(),
        permission.getLabel(),
        permission.getDescription(),
        permission.getCategory() != null
            ? PermissionCategoryDTO.fromEntity(permission.getCategory())
            : null);
  }
}
