package lk.gov.mohe.adminsystem.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrUpdateRoleRequestDto(
    @NotBlank(message = "Role name cannot be blank.") String name,
    String description,
    @NotNull List<String> permissions) {}
