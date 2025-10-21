package lk.gov.mohe.adminsystem.user;

import jakarta.validation.constraints.NotNull;

public record ResetPasswordRequestDto(
    @NotNull(message = "User ID is required") Integer userId) {}
