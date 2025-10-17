package lk.gov.mohe.adminsystem.role;

import java.util.List;

public record CreateOrUpdateRoleRequestDto(
    String name, String description, List<String> permissions) {}
