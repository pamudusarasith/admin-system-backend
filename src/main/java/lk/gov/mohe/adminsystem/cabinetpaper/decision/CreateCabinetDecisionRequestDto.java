package lk.gov.mohe.adminsystem.cabinetpaper.decision;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateCabinetDecisionRequestDto(
    @NotNull(message = "Decision type is required") DecisionTypeEnum decisionType,
    @NotBlank(message = "Decision text is required") String decisionText,
    @NotNull(message = "Decision date is required") LocalDate decisionDate) {}
