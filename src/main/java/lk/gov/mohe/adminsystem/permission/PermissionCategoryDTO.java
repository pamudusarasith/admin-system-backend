package lk.gov.mohe.adminsystem.permission;

import java.util.List;
import java.util.stream.Collectors;

public record PermissionCategoryDTO(
        Integer id,
        String name,
        Integer parentId
//        List<PermissionDTO> permissions
) {
    public static PermissionCategoryDTO fromEntity(PermissionCategory category) {
        return new PermissionCategoryDTO(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null
//                category.getPermissions() != null ? category.getPermissions().stream().map(PermissionDTO::fromEntity).collect(Collectors.toList()) : List.of()
        );
    }
}
