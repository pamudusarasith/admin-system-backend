package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.attachment.Attachment;
import lk.gov.mohe.adminsystem.attachment.AttachmentMapper;
import lk.gov.mohe.adminsystem.attachment.AttachmentRepository;
import lk.gov.mohe.adminsystem.attachment.ParentTypeEnum;
import lk.gov.mohe.adminsystem.division.DivisionMapper;
import lk.gov.mohe.adminsystem.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", uses = {UserMapper.class, DivisionMapper.class,
    AttachmentMapper.class})
public abstract class LetterMapper {
    @Autowired
    AttachmentRepository attachmentRepository;
    ParentTypeEnum letterParentType = ParentTypeEnum.LETTER;

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
        CreateOrUpdateLetterRequestDto request, @MappingTarget Letter letter
    );

    @Mapping(target = "noOfAttachments", expression = "java( attachmentRepository" +
        ".countByParentIdAndParentType(letter.getId(), letterParentType) )")
    abstract LetterMinDto toLetterMinDto(Letter letter);

    abstract LetterFullDto toLetterFullDto(Letter letter,
                                           List<Attachment> attachments,
                                           List<LetterEvent> events);

    SenderDetailsDto toSenderDetailsDto(Map<String, Object> senderDetails) {
        return new SenderDetailsDto(
            (String) senderDetails.get("name"),
            (String) senderDetails.get("address"),
            (String) senderDetails.get("email"),
            (String) senderDetails.get("phone_number")
        );
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
            (String) receiverDetails.get("division_name")
        );
    }

    Map<String, Object> toReceiverDetailsMap(ReceiverDetailsDto receiverDetails) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", receiverDetails.name());
        map.put("designation", receiverDetails.designation());
        map.put("division_name", receiverDetails.divisionName());
        return map;
    }

    abstract LetterEventDto toLetterEventDto(LetterEvent event);
}
