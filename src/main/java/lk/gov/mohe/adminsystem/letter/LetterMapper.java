package lk.gov.mohe.adminsystem.letter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lk.gov.mohe.adminsystem.attachment.*;
import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.division.DivisionDto;
import lk.gov.mohe.adminsystem.division.DivisionMapper;
import lk.gov.mohe.adminsystem.user.User;
import lk.gov.mohe.adminsystem.user.UserDto;
import lk.gov.mohe.adminsystem.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    componentModel = "spring",
    uses = {UserMapper.class, DivisionMapper.class, AttachmentMapper.class})
public abstract class LetterMapper {
  @Autowired protected AttachmentMapper attachmentMapper;
  @Autowired protected DivisionMapper divisionMapper;
  @Autowired protected AttachmentRepository attachmentRepository;
  ParentTypeEnum letterParentType = ParentTypeEnum.LETTER;
  @Autowired private UserMapper userMapper;

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "assignedDivision", ignore = true)
  @Mapping(target = "assignedUser", ignore = true)
  @Mapping(target = "isAcceptedByUser", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  abstract Letter toEntity(CreateOrUpdateLetterRequestDto request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "assignedDivision", ignore = true)
  @Mapping(target = "assignedUser", ignore = true)
  @Mapping(target = "isAcceptedByUser", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  abstract void updateEntityFromCreateOrUpdateLetterRequestDto(
      CreateOrUpdateLetterRequestDto request, @MappingTarget Letter letter);

  @Mapping(target = "assignedUser", source = "assignedUser", qualifiedByName = "toUserDtoMin")
  @Mapping(
      target = "noOfAttachments",
      expression =
          "java( attachmentRepository.countByParentIdAndParentType(letter.getId(), letterParentType) )")
  @Mapping(target = "attachments", ignore = true)
  @Mapping(target = "events", ignore = true)
  abstract LetterDto toLetterDtoMin(Letter letter);

  @Mapping(
      target = "assignedUser",
      source = "letter.assignedUser",
      qualifiedByName = "toUserDtoMin")
  @Mapping(target = "noOfAttachments", ignore = true)
  abstract LetterDto toLetterDtoFull(
      Letter letter, List<Attachment> attachments, List<LetterEvent> events);

  SenderDetailsDto toSenderDetailsDto(Map<String, Object> senderDetails) {
    return new SenderDetailsDto(
        (String) senderDetails.get("name"),
        (String) senderDetails.get("address"),
        (String) senderDetails.get("email"),
        (String) senderDetails.get("phone_number"));
  }

  Map<String, Object> toSenderDetailsMap(SenderDetailsDto senderDetails) {
    Map<String, Object> map = new HashMap<>();
    map.put("name", senderDetails.name());
    map.put("address", senderDetails.address());
    map.put("email", senderDetails.email());
    map.put("phone_number", senderDetails.phoneNumber());
    return map;
  }

  ReceiverDetailsDto toReceiverDetailsDto(Map<String, Object> receiverDetails) {
    return new ReceiverDetailsDto(
        (String) receiverDetails.get("name"),
        (String) receiverDetails.get("designation"),
        (String) receiverDetails.get("division_name"));
  }

  Map<String, Object> toReceiverDetailsMap(ReceiverDetailsDto receiverDetails) {
    Map<String, Object> map = new HashMap<>();
    map.put("name", receiverDetails.name());
    map.put("designation", receiverDetails.designation());
    map.put("division_name", receiverDetails.divisionName());
    return map;
  }

  @Mapping(target = "user", source = "user", qualifiedByName = "toUserDtoMin")
  abstract LetterEventDto toLetterEventDto(LetterEvent event);

  EventDetailsDto toEventDetailsDto(Map<String, Object> eventDetails) {
    if (eventDetails == null || eventDetails.isEmpty()) {
      return null;
    }

    String newStatus = (String) eventDetails.get("newStatus");
    String content = (String) eventDetails.get("content");

    List<AttachmentDto> attachments = null;
    Object attachmentsObj = eventDetails.get("attachments");
    if (attachmentsObj instanceof List<?>) {
      @SuppressWarnings("unchecked")
      List<Attachment> attachmentEntities = (List<Attachment>) attachmentsObj;
      attachments = attachmentEntities.stream().map(attachmentMapper::toDto).toList();
    }

    DivisionDto division = null;
    Object divisionObj = eventDetails.get("division");
    if (divisionObj instanceof Division) {
      division = divisionMapper.toDto((Division) divisionObj);
    }

    UserDto user = null;
    Object userObj = eventDetails.get("user");
    if (userObj instanceof User) {
      user = userMapper.toUserDtoMin((User) userObj);
    }

    String reason = (String) eventDetails.get("reason");

    return new EventDetailsDto(newStatus, content, attachments, division, user, reason);
  }
}
