package lk.gov.mohe.adminsystem.letter;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lk.gov.mohe.adminsystem.attachment.AttachmentDto;
import lk.gov.mohe.adminsystem.division.DivisionDto;
import lk.gov.mohe.adminsystem.user.UserDto;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventDetailsDto(
    String newStatus,
    String previousStatus,
    PriorityEnum newPriority,
    PriorityEnum previousPriority,
    String content,
    List<AttachmentDto> attachments,
    DivisionDto division,
    UserDto user,
    String reason,
    String reference,
    String senderDetailsName,
    String senderDetailsAddress,
    String senderDetailsEmail,
    String senderDetailsPhoneNumber,
    String receiverDetailsName,
    String receiverDetailsDesignation,
    String receiverDetailsDivisionName,
    String sentDate,
    String receivedDate,
    ModeOfArrivalEnum modeOfArrival,
    String subject,
    String updatedContent,
    PriorityEnum updatedPriority
) {}
