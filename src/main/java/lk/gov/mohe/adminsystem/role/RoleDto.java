package lk.gov.mohe.adminsystem.role;

import java.util.List;

public record RoleDto(
        Integer id,
        String name,
        String description,
        List<String> permissions,
        long userCount
) {
}
