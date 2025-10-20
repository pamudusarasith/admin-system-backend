package lk.gov.mohe.adminsystem.cabinetpaper;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lk.gov.mohe.adminsystem.attachment.AttachmentDto;
import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategoryDto;
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
    Long noOfAttachments,
    List<AttachmentDto> attachments,
    String createdAt,
    String updatedAt) {}
