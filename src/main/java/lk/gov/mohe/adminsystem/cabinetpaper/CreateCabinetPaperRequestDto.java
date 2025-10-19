package lk.gov.mohe.adminsystem.cabinetpaper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCabinetPaperRequestDto(
    @NotBlank(message = "Reference ID is required") @Size(max = 50) String referenceId,
    @NotBlank(message = "Subject is required") @Size(max = 255) String subject,
    String summary,
    @NotNull(message = "Category is required") Integer categoryId,
    @NotNull(message = "Status is required") CabinetPaperStatusEnum status) {}
