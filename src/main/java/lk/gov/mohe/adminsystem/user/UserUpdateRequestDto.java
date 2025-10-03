package lk.gov.mohe.adminsystem.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserUpdateRequestDto(

        @NotBlank(message = "Full name must not be blank")
        @JsonProperty("username")
        String username,

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Invalid email format")
        @JsonProperty("email")
        String email,

        @NotBlank(message = "Full name must not be blank")
        @JsonProperty("fullName")
        String fullName,

        @NotBlank(message = "Phone number must not be blank")
        @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
        @JsonProperty("phoneNumber")
        String phoneNumber,

        @NotNull(message = "Role must not be null")
        @JsonProperty("role")
        String role,

        @NotNull(message = "Division must not be null")
        @JsonProperty("division")
        String division,

        @NotNull(message = "Active status must not be null")
        @JsonProperty("isActive")
        Boolean isActive
) {
}
