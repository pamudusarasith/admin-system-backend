package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.attachment.AttachmentDto;
import lk.gov.mohe.adminsystem.division.DivisionDto;
import lk.gov.mohe.adminsystem.user.UserMinDto;

import java.util.List;

public record LetterFullDto(
    Integer id,
    String reference,
    SenderDetailsDto senderDetails,
    ReceiverDetailsDto receiverDetails,
    String sentDate,
    String receivedDate,
    ModeOfArrivalEnum modeOfArrival,
    String subject,
    String content,
    String priority,
    String status,
    DivisionDto assignedDivision,
    UserMinDto assignedUser,
    Boolean isAcceptedByUser,
    List<AttachmentDto> attachments,
    List<LetterEventDto> events,
    String createdAt,
    String updatedAt
) {
}
