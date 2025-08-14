package lk.gov.mohe.adminsystem.security;

import jakarta.validation.constraints.NotEmpty;

public record LoginRequestDto(
    @NotEmpty
    String username,
    @NotEmpty
    String password
) {
}
