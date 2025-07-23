package lk.gov.mohe.adminsystem.letter;

import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.Map;

public record SenderDetailsDto(
    @NotBlank
    String name,

    String address,

    String email,

    String phoneNumber
) {
}
