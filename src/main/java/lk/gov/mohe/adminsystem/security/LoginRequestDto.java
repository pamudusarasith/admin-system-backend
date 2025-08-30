package lk.gov.mohe.adminsystem.security;

public record LoginRequestDto(
    String username,
    String password
) {
}
