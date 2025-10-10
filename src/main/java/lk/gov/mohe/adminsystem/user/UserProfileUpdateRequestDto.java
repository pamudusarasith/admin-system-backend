package lk.gov.mohe.adminsystem.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record UserProfileUpdateRequestDto(
    @NotBlank(message = "Full name must not be blank") @JsonProperty("fullName") String fullName,
    @NotBlank(message = "Email must not be blank")
        @Email(message = "Invalid email format")
        @JsonProperty("email")
        String email,
    @NotBlank(message = "Phone number must not be blank")
        @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
        @JsonProperty("phoneNumber")
        String phoneNumber) {}
