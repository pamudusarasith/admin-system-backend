package lk.gov.mohe.adminsystem.letter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LetterMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedDivision", ignore = true)
    @Mapping(target = "assignedUser", ignore = true)
    @Mapping(target = "isAcceptedByUser", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(
        target = "senderDetails", expression = "java(request.senderDetails().toMap())")
    Letter toEntity(CreateOrUpdateLetterRequestDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedDivision", ignore = true)
    @Mapping(target = "assignedUser", ignore = true)
    @Mapping(target = "isAcceptedByUser", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(
        target = "senderDetails", expression = "java(request.senderDetails().toMap())")
    void updateEntityFromCreateOrUpdateLetterRequestDto(
        CreateOrUpdateLetterRequestDto request, @MappingTarget Letter letter
    );
}
