package lk.gov.mohe.adminsystem.user;

import jakarta.validation.constraints.*;

public record CreateUserRequest(
    @NotBlank(message = "Username must not be blank")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
    @NotBlank(message = "Email must not be blank") @Email(message = "Invalid email format")
        String email,
    @NotNull(message = "Division Id cannot be null") Integer divisionId,
    @NotNull(message = "Role Id cannot be null") Integer roleId) {}
