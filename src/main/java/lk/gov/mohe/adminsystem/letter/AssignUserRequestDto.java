package lk.gov.mohe.adminsystem.letter;

import jakarta.validation.constraints.NotNull;

public record AssignUserRequestDto(@NotNull(message = "User ID is required") Integer userId) {}
