package lk.gov.mohe.adminsystem.division;

import jakarta.validation.constraints.NotBlank;

public record CreateOrUpdateDivisionRequestDto(@NotBlank String name, String description) {}
