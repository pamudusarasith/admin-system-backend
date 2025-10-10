package lk.gov.mohe.adminsystem.letter;

import jakarta.validation.constraints.NotNull;

public record AssignDivisionRequestDto(
    @NotNull(message = "Division ID is required") Integer divisionId) {}
