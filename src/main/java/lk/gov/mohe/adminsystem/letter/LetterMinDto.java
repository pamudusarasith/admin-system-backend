package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.division.DivisionDto;
import lk.gov.mohe.adminsystem.user.UserMinDto;

public record LetterMinDto(
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
    Long noOfAttachments,
    String createdAt,
    String updatedAt
) {
}
