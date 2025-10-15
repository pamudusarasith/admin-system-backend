package lk.gov.mohe.adminsystem.division;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DivisionMapper {
  DivisionDto toDto(Division division);

  @Mapping(target = "id", ignore = true)
  Division createOrUpdateRequestDtoToDivision(CreateOrUpdateDivisionRequestDto dto);
}
