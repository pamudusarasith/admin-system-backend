package lk.gov.mohe.adminsystem.cabinetpaper;

import com.fasterxml.jackson.annotation.JsonInclude;
import lk.gov.mohe.adminsystem.user.UserDto;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CabinetPaperDto(
    Integer id,
    String referenceId,
    String subject,
    String summary,
    CabinetPaperCategoryDto category,
    CabinetPaperStatusEnum status,
    UserDto submittedByUser,
    String createdAt,
    String updatedAt) {}
