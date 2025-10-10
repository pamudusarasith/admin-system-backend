package lk.gov.mohe.adminsystem.letter;

import jakarta.validation.constraints.NotBlank;

public record SenderDetailsDto(
    @NotBlank String name, String address, String email, String phoneNumber) {}
