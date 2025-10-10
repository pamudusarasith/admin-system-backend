package lk.gov.mohe.adminsystem.letter;

import com.fasterxml.jackson.annotation.JsonInclude;
import lk.gov.mohe.adminsystem.attachment.AttachmentDto;
import lk.gov.mohe.adminsystem.division.DivisionDto;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventDetailsDto(
    String newStatus,
    String content,
    List<AttachmentDto> attachments,
    DivisionDto assignedDivision
) {
}
