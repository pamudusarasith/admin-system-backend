package lk.gov.mohe.adminsystem.permission;

import java.util.List;

public record SubCategoryDTO(Integer id, String name, List<PermissionDTO> permissions) {}
