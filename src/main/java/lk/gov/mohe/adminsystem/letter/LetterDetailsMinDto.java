package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.division.DivisionDto;
import lk.gov.mohe.adminsystem.user.UserDto;

public record LetterDetailsMinDto(
    Integer id,
    String reference,
    SenderDetailsDto senderDetails,
    String sentDate,
    String receivedDate,
    ModeOfArrivalEnum modeOfArrival,
    String subject,
    String content,
    PriorityEnum priority,
    StatusEnum status,
    DivisionDto assignedDivision,
    UserDto assignedUser,
    Boolean isAcceptedByUser
) {
}
