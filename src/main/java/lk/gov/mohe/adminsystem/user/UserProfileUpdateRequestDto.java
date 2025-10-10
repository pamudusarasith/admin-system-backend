package lk.gov.mohe.adminsystem.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserProfileUpdateRequestDto(
    @NotBlank(message = "Full name must not be blank") String fullName,
    @NotBlank(message = "Email must not be blank") @Email(message = "Invalid email format")
        String email,
    @NotBlank(message = "Phone number must not be blank")
        @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
        String phoneNumber) {}
