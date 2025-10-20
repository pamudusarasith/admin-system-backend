package lk.gov.mohe.adminsystem.user;

import jakarta.validation.constraints.*;

public record AccountSetupRequestDto(
    @NotBlank(message = "Full name must not be blank") String fullName,
    @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a valid email address")
        String email,
    @NotBlank(message = "Phone number must not be blank")
        @Pattern(regexp = "\\d{10}", message = "Invalid phone number format. Use format: 0123456789")
        String phoneNumber,
    String oldPassword,
    @NotBlank(message = "New password must not be blank")
        @Size(min = 6, message = "New password must be at least 6 characters long")
        String newPassword) {}
