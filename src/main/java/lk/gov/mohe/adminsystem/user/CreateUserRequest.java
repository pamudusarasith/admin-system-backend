package lk.gov.mohe.adminsystem.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank(message = "Username must not be blank")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
    @NotBlank(message = "Email must not be blank") @Email(message = "Invalid email format")
        String email,
    @NotBlank(message = "Full name must not be blank") String fullName,
    @NotBlank(message = "Phone number must not be blank")
        @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
        String phoneNumber,
    @NotBlank(message = "Role must not be blank") String role,
    @NotBlank(message = "Division must not be blank") String division) {}
