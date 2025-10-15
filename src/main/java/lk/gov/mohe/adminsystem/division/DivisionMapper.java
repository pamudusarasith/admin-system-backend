package lk.gov.mohe.adminsystem.division;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DivisionMapper {
  DivisionDto toDto(Division division);

  @Mapping(target = "id", ignore = true)
  Division dtoToDivision(CreateOrUpdateDivisionRequestDto dto);

  @Mapping(target = "id", ignore = true)
  void updateDivisionFromDto(
      CreateOrUpdateDivisionRequestDto dto, @MappingTarget Division division);
}
