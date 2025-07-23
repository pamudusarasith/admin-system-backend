package lk.gov.mohe.adminsystem.user;

public record UserDto(
    Integer id,
    String username,
    String email,
    String fullName,
    String phoneNumber,
    String role,
    String division,
    Boolean isActive
) {
}
