package lk.gov.mohe.adminsystem.cabinetpaper.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCabinetPaperCategoryRequestDto(
    @NotBlank(message = "Category name is required") @Size(max = 100) String name,
    String description) {}
