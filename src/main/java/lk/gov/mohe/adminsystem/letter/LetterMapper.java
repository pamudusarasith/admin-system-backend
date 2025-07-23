package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.attachment.AttachmentRepository;
import lk.gov.mohe.adminsystem.attachment.ParentTypeEnum;
import lk.gov.mohe.adminsystem.division.DivisionMapper;
import lk.gov.mohe.adminsystem.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring", uses = {UserMapper.class, DivisionMapper.class})
public abstract class LetterMapper {
    @Autowired
    AttachmentRepository attachmentRepository;
    ParentTypeEnum letterParentType = ParentTypeEnum.LETTER;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedDivision", ignore = true)
    @Mapping(target = "assignedUser", ignore = true)
    @Mapping(target = "isAcceptedByUser", ignore = true)
    @Mapping(target = "status", ignore = true)
    abstract Letter toEntity(CreateOrUpdateLetterRequestDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedDivision", ignore = true)
    @Mapping(target = "assignedUser", ignore = true)
    @Mapping(target = "isAcceptedByUser", ignore = true)
    @Mapping(target = "status", ignore = true)
    abstract void updateEntityFromCreateOrUpdateLetterRequestDto(
        CreateOrUpdateLetterRequestDto request, @MappingTarget Letter letter
    );

    @Mapping(target = "noOfAttachments", expression = "java( attachmentRepository" +
        ".countByParentIdAndParentType(letter.getId(), letterParentType) )")
    abstract LetterDetailsMinDto toLetterDetailsMinDto(Letter letter);

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
}
