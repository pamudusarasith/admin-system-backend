package lk.gov.mohe.adminsystem.letter;

import com.fasterxml.jackson.annotation.JsonInclude;
import lk.gov.mohe.adminsystem.attachment.AttachmentDto;
import lk.gov.mohe.adminsystem.division.DivisionDto;
import lk.gov.mohe.adminsystem.user.UserDto;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LetterDto(
    Integer id,
    String reference,
    SenderDetailsDto senderDetails,
    ReceiverDetailsDto receiverDetails,
    String sentDate,
    String receivedDate,
    ModeOfArrivalEnum modeOfArrival,
    String subject,
    String content,
    PriorityEnum priority,
    StatusEnum status,
    DivisionDto assignedDivision,
    UserDto assignedUser,
    Boolean isAcceptedByUser,
    Long noOfAttachments,
    List<AttachmentDto> attachments,
    List<LetterEventDto> events,
    String createdAt,
    String updatedAt
) {
}
