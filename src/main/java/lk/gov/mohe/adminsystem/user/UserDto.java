package lk.gov.mohe.adminsystem.user;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDto(
    Integer id,
    String username,
    String email,
    String fullName,
    String phoneNumber,
    String role,
    String division,
    Boolean isActive) {}
