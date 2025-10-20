package lk.gov.mohe.adminsystem.letter;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record SenderDetailsDto(
    @NotBlank String name,
    String address,
    String email,
    @JsonProperty("phone_number") String phoneNumber) {}
